import time
from pypresence import Presence

# ID вашего клиента (получить из Discord Developer Portal)
client_id = '1364606826473197578'

# Подключаемся к Discord
RPC = Presence(client_id)
RPC.connect()

# Обновляем активность
RPC.update(state="456", details="123", large_image="minecraft", small_image="small_image")

# Ожидаем 60 секунд, чтобы обновление активности было видно
time.sleep(60)

# Закрываем соединение с Discord
RPC.close()
