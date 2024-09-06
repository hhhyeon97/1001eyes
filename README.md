# 1001eyes


<details>
<summary>목차</summary>

- [Overview](#Overview)
- [ERD](#ERD)
- [Tech Stack](#Tech-Stack)
- [Main Feature](#Main-Feature)
- [성능 개선](#성능-개선)

[//]: # (- [Trouble Shooting]&#40;#Trouble-Shooting&#41;)

</details>


## Overview

1001eyes는 한정판 제품을 선착순 구매하는 상황을 기반으로 한 이커머스 서비스입니다. <br>
MSA 구조를 적용하여 모듈 간 독립성을 강화했으며, 대용량 트래픽을 처리하기 위해 Redis 캐싱, 분산 락 등을 활용했습니다. <br>
주요 목표는 구매 프로세스를 설계하고, 안정적인 성능을 유지하면서 재고 관리를 하는 것입니다. <br>

- 개발 기간: 2024/08/07 ~ 2024/09/04 (4주)
- [API 명세서](https://documenter.getpostman.com/view/33051866/2sA3s7ho4p)

## ERD
![img.png](etc/image/img.png)

## Tech Stack

| **Tech**       | **Usage**                  |
|----------------|----------------------------|
| **Language**   | Java 21                    |
| **Framework**  | Spring Boot 3.3.2          |
| **Build**      | Gradle                     |
| **Database**   | MySQL 8.0.30               |
| **ORM**        | Spring Data JPA            |
| **Cache**      | Redis (Redisson 3.21.0)    |
| **Cloud**      | Spring Cloud 2023.0.3      |
| **Service Discovery** | Spring Eureka       |
| **API Gateway**| Spring Cloud Gateway 4.1.5 |
| **Library**    | Feign Client               |
| **Library**    | JJWT (0.11.5)              |
| **Library**    | Spring Boot Mail (3.1.2)   |
| **Load Testing** | JMeter                   |
| **DevOps**     | Docker (25.0.3)            |

## Main Feature

## 성능 개선

[//]: # (## Trouble Shooting)

<div align="right">

[맨 위로](#1001eyes)

</div>