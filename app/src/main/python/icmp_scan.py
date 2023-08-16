import socket
from ping3 import ping, verbose_ping



def main(start_range, end_range):
    # Set the multicast group and port for SSDP
    multicast_group = '239.255.255.250'
    port = 1900
    output_text ="Scan Result: \n"

    # Create a dictionary to store unique devices
    unique_devices = {}

    # Define the SSDP discovery message
    ssdp_request = (
        'M-SEARCH * HTTP/1.1\r\n'
        'Host: {}:{}\r\n'
        'Man: "ssdp:discover"\r\n'
        'MX: 1\r\n'
        'ST: ssdp:all\r\n'
        '\r\n'
    ).format(multicast_group, port)

    # Create a UDP socket for sending and receiving SSDP messages
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(5)  # Set a timeout for receiving responses

    # Set the socket to allow broadcasting
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    # Send the SSDP request
    sock.sendto(ssdp_request.encode(), (multicast_group, port))

    # Receive and process SSDP responses
    print("start process")
    try:
        while True:
            data, addr = sock.recvfrom(4096)
            response = f"Received response from {addr[0]}:\n{data.decode()}"
            #print(response)

            # Extract the hostname and IP address from the response
            hostname = data.decode().split("LOCATION:")[1].split(":")[1].lstrip().split("/")[2]
            ip_address = addr[0]

            # Store unique devices in the dictionary
            if ip_address not in unique_devices:
                unique_devices[ip_address] = hostname

    except socket.timeout:
        pass

    # Close the socket
    sock.close()

    # Generate the output text with unique devices
    for ip, hostname in unique_devices.items():
        output_text += f"Hostname: {hostname}, IP: {ip}\n"
    #IMCP Ping
    local_ip = ""
    local_ip = get_local_ip()
    #print(local_ip)
    base_ip = get_subnet_ip(local_ip) + "."
    #print(base_ip)
    result = ""

    # Define the number of threads/processes
    for i in range(start_range, end_range):
        #print(start_range,end_range)
        ip_address = base_ip + str(i)
        print(i)
        response_time = ping(ip_address)

        if response_time is not False and response_time is not None:
            print(f"Host {ip_address} is reachable. Response time: {response_time} ms {get_hostname(ip_address)}")
            result += f"Host:{get_hostname(ip_address)} + IP: {ip_address} "
        else:
            pass

    return result


def get_hostname(host_address):
    try:
        hostname = socket.gethostbyaddr(host_address)
        return hostname[0]
    except socket.herror:
        return "Hostname not found"

def check_host(ip_address):
    response_time = ping(ip_address)
    if response_time is not False and response_time is not None:
        print(f"Host {ip_address} is reachable. Response time: {response_time} ms {get_hostname(ip_address)}")
        return f"Host:{get_hostname(ip_address)} + IP: {ip_address}"
    return None

def get_local_ip():
    try:
        # Create a socket object
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))  # Connect to a known external server

        # Get the local IP address
        local_ip = s.getsockname()[0]

        s.close()  # Close the socket

        return local_ip
    except Exception as e:
        print("Error:", e)
        return None

def get_subnet_ip(ip):
    parts = ip.split('.')
    first_three_parts = '.'.join(parts[:3])
    #print(first_three_parts)
    return first_three_parts

if __name__ == "__main__":
    start_range = 1  # Start IP range Default
    end_range = 255  # End IP range
    result = main(1, 20)
    print(result)
