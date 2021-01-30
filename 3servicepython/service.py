from http.server import HTTPServer, BaseHTTPRequestHandler
import json

data = {'状态': '调用服务三成功',"版本":"v2","语言":"python"}


# host = ('localhost', 7777)
host = ('0.0.0.0', 7777)


class Resquest(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header("Access-Control-Allow-Origin","*")
        self.send_header("Cache-Control","no-cache")
        self.end_headers()
        self.wfile.write(json.dumps(data,ensure_ascii=False).encode())




if __name__ == '__main__':
    server = HTTPServer(host, Resquest)
    print("Starting Server....")
    server.serve_forever()
