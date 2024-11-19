import socket
import struct
import time

def send_data_continuously(host, port, message, interval=0.0001):
    try:
        # 创建 TCP 套接字并连接服务器
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            sock.connect((host, port))
            print("Connected to server.")

            # 持续发送数据
            while True:
                # 将消息转换为字节格式
                message_bytes = message.encode('utf-8')
                
                # 计算消息长度，并构建协议数据
                message_length = len(message_bytes)
                packed_data = struct.pack("!I", message_length) + message_bytes
                
                # 发送数据
                sock.sendall(packed_data)
                print(f"Sent: {message} (Length: {message_length})")
                

                # 接收服务器的响应
                # 首先接收4字节的响应长度
                response_length_data = sock.recv(4)
                if not response_length_data:
                    print("No response from server. Closing connection.")
                    break

                # 解包获得响应内容的长度
                response_length = struct.unpack("!I", response_length_data)[0]
                
                # 接收指定长度的响应内容
                response_data = sock.recv(response_length).decode('utf-8')
                print(f"Received: {response_data} (Length: {response_length})")
                
                # 等待设定的间隔时间
                time.sleep(interval)
                
    except (ConnectionRefusedError, ConnectionResetError):
        print("Connection failed or was reset by server.")
    except KeyboardInterrupt:
        print("Stopped by user.")

# 示例用法
host = '127.0.0.1'  # 服务器地址
port = 6789         # 服务器端口
message = "Hello, TCP server!"  # 要发送的消息
interval = 2        # 发送间隔时间，单位为秒

send_data_continuously(host, port, message)
