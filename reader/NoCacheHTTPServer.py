import http.server

class NoCacheHTTPRequestHandler(
    http.server.SimpleHTTPRequestHandler
):
    def send_response_only(self, code, message=None):
        super().send_response_only(code, message)
        self.send_header('Cache-Control',
            'no-store, must-revalidate')
        self.send_header('Expires', '0')

if __name__ == '__main__':
    http.server.test(
        HandlerClass=NoCacheHTTPRequestHandler,
        bind='localhost',
        port=5050
    )
