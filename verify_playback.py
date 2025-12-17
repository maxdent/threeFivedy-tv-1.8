import requests
from bs4 import BeautifulSoup
import hashlib
import hmac
import time
import json
import binascii
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad

# Configuration matches the App's updated state
BASE_URL = "https://www.5j7o1g3g9z6.shop"
USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"
PLAY_URL_SERVER = "" # Will be fetched

# Keys from HttpDataRepository.kt
IV = "d11324dcscfe16c0".encode('utf-8')
KEY = "55cc5c42a943afdc".encode('utf-8')
MAGIC_SIGN_STRING = "55ca5c4d11424dcecfe16c08a943afdc"

def md5(text):
    return hashlib.md5(text.encode('utf-8')).hexdigest()

def get_app_key():
    # Updated domain as per recent fix
    return md5("www.5j7o1g3g9z6.shop")

def get_client_key():
    return md5(USER_AGENT)

def get_request_token():
    # Updated as per recent fix
    return md5("https://www.5j7o1g3g9z6.shop")

def aes_encrypt(data):
    cipher = AES.new(KEY, AES.MODE_CBC, IV)
    ct_bytes = cipher.encrypt(pad(data.encode('utf-8'), AES.block_size))
    return binascii.hexlify(ct_bytes).decode('utf-8').upper()

def aes_decrypt(hex_data):
    cipher = AES.new(KEY, AES.MODE_CBC, IV)
    ct = binascii.unhexlify(hex_data)
    pt = unpad(cipher.decrypt(ct), AES.block_size)
    return pt.decode('utf-8')

def fetch_home_page():
    print(f"Fetching home page: {BASE_URL}")
    resp = requests.get(BASE_URL, headers={"User-Agent": USER_AGENT})
    resp.raise_for_status()
    return resp.text

def parse_first_video_id(html):
    soup = BeautifulSoup(html, 'html.parser')
    # Try to find a video link. Structure based on getHomePage parsing logic
    # ".module .module-main .module-item" -> href
    item = soup.select_first(".module .module-main .module-item")
    if not item:
        item = soup.select_first("a.module-poster-item") # Fallback
    
    if not item:
        raise Exception("Could not find any video on home page")
    
    href = item['href']
    # href format: /voddetail/1234.html or similar
    # getIdFromUrl logic: substring last / + 1 to last .
    vid_id = href.split('/')[-1].split('.')[0]
    print(f"Found Video ID: {vid_id} (href: {href})")
    return vid_id

def fetch_detail_page(vid_id):
    url = f"{BASE_URL}/voddetail/{vid_id}.html"
    print(f"Fetching detail page: {url}")
    resp = requests.get(url, headers={"User-Agent": USER_AGENT})
    resp.raise_for_status()
    return resp.text

def parse_first_episode_id(html):
    soup = BeautifulSoup(html, 'html.parser')
    # Structure based on getDetailPage logic
    # ".module-play-list-content a"
    link = soup.select_first(".module-play-list-content a")
    if not link:
        raise Exception("Could not find any episode links on detail page")
    
    href = link['href']
    # href format: /vodplay/1234-1-1.html
    ep_id = href.split('/')[-1].split('.')[0]
    print(f"Found Episode ID: {ep_id} (href: {href})")
    return ep_id

def fetch_player_server():
    url = f"{BASE_URL}/player.html?v=1"
    print(f"Fetching player server config: {url}")
    resp = requests.get(url, headers={"User-Agent": USER_AGENT})
    # Regex logic: server_url\s*=\s*['"]([\w:/.]+)['"]
    import re
    match = re.search(r"server_url\s*=\s*['\"]([\w:/.]+)['\"]", resp.text)
    if match:
        server = match.group(1)
        print(f"Found Server URL: {server}")
        return server
    return ""

def query_video_url(ep_id, server_url):
    # 1. Get encrypted URL config
    # Logic: voddisp/id/{0}/sid/{1}/nid/{2}.html
    parts = ep_id.split('-')
    config_url = f"{BASE_URL}/voddisp/id/{parts[0]}/sid/{parts[1]}/nid/{parts[2]}.html"
    print(f"Fetching config URL: {config_url}")
    
    resp = requests.get(config_url, headers={"User-Agent": USER_AGENT})
    resp.raise_for_status()
    data = resp.json()
    encrypt_url = data.get('url')
    if not encrypt_url:
        raise Exception("Config response did not contain 'url' field")
    print(f"Got encrypted URL segment: {encrypt_url}")

    # 2. Encrypt it locally
    pack_string = aes_encrypt(encrypt_url)
    print(f"Generated pack string: {pack_string[:20]}...")

    # 3. Generate Signature
    timestamp = int(time.time())
    # digestHex(serverUrl + "GET" + timestamp + "55ca5c4d11424dcecfe16c08a943afdc")
    sig_base = f"{server_url}GET{timestamp}{MAGIC_SIGN_STRING}"
    url_md5 = md5(sig_base)
    
    # HMac(HmacAlgorithm.HmacSHA256, urlMd5.toByteArray()).digestHex(packString)
    # Note: verify if Kotlin's HMac uses hex string or raw bytes for key?
    # Kotlin: HMac(HmacAlgorithm.HmacSHA256, urlMd5.toByteArray())
    # urlMd5.toByteArray() in Kotlin (Hutool/Java) usually gets bytes of the string unless specified. 
    # Let's assume it is bytes of the UTF-8 string of the hex digest.
    key_bytes = url_md5.encode('utf-8')
    signature = hmac.new(key_bytes, pack_string.encode('utf-8'), hashlib.sha256).hexdigest()
    
    print(f"generated signature: {signature}")

    # 4. Request Play URL
    req_url = f"{server_url}/get_play_url"
    app_key = get_app_key()
    client_key = get_client_key()
    req_token = get_request_token()
    access_token_base = req_url.split("://")[1]
    access_token = md5(access_token_base)

    params = {
        "app_key": app_key,
        "client_key": client_key,
        "request_token": req_token,
        "access_token": access_token
    }
    
    headers = {
        "User-Agent": USER_AGENT,
        "X-PLAYER-TIMESTAMP": str(timestamp),
        "X-PLAYER-SIGNATURE": signature,
        "X-PLAYER-METHOD": "GET",
        "X-PLAYER-PACK": pack_string
    }

    print(f"Requesting play URL from: {req_url}")
    print(f"Params: {params}")
    
    final_resp = requests.get(req_url, params=params, headers=headers)
    print(f"Response Status: {final_resp.status_code}")
    print(f"Response Body: {final_resp.text}")
    
    if final_resp.status_code != 200:
        raise Exception(f"Server returned error: {final_resp.status_code}")

    # 5. Decrypt
    # Body is hex string? Or JSON?
    # Kotlin: body?.string()?.let { cipher.doFinal(...) }
    # Kotlin logic: it.chunked(2).map { it.toInt(0x10).toByte() } -> it treats body as Hex String
    encrypted_body = final_resp.text
    decrypted_json_str = aes_decrypt(encrypted_body)
    print(f"Decrypted JSON: {decrypted_json_str}")
    
    final_data = json.loads(decrypted_json_str)
    video_url = final_data.get('data', {}).get('url')
    print(f"FINAL VIDEO URL: {video_url}")
    return video_url

def main():
    try:
        html = fetch_home_page()
        vid_id = parse_first_video_id(html)
        detail_html = fetch_detail_page(vid_id)
        ep_id = parse_first_episode_id(detail_html)
        
        server_url = fetch_player_server()
        if not server_url:
            # Fallback based on Constants.PLAY_URL_SERVER default logic if dynamic fetch fails?
            # But in Repos, it just returns "" if not found.
            # Let's hardcode one if we know it or fail.
            print("Warning: Could not fetch server_url dynamically.")
            # Based on code, it might default or be empty. 
            # In Constants.kt, var PLAY_URL_SERVER: String = ""
            # So if it's empty, the request will fail.
            return

        query_video_url(ep_id, server_url)
        print("\nSUCCESS: Verification Passed")
    except Exception as e:
        print(f"\nFAILURE: Verification Failed - {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()
