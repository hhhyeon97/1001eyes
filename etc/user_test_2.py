import csv

# CSV 파일 생성
with open('user_data.csv', mode='w', newline='', encoding='utf-8') as file:
    writer = csv.writer(file)
    
    # 헤더 추가
    writer.writerow(["username", "password", "email", "phone", "name", "address", "addressDetail", "zipcode"])
    
    # 1000명의 사용자 데이터 추가
    for i in range(1, 10001):
        username = f"t0830_{i}"
        password = "1234"
        email = f"t0830_{i}@gmail.com"
        phone = "010-1234-5678"
        name = "솔"
        address = "서울특별시 마포구"
        address_detail = "동교로51길 133"
        zipcode = "16305"
        
        writer.writerow([username, password, email, phone, name, address, address_detail, zipcode])