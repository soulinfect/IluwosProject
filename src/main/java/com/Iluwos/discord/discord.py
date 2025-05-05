import time
from pypresence import Presence

client_id = '1366285642111254648'  

RPC = Presence(client_id)
RPC.connect()

print("RPC запущена. Нажми Ctrl+C для выхода.")

try:
    while True:  
        current_time = int(time.time())  
        
        RPC.update(
            details="123",
            state="456",
            large_image="minecraft",  
            small_image="hypixel",  
            start=current_time
        )
        
        time.sleep(10)  

except KeyboardInterrupt:  
    print("\nЗавершение работы...")
finally:
    RPC.close() 