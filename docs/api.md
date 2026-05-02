# API Documentation

Base URL:

```text
http://localhost:8081
```

Most protected APIs require:

```http
authorization: {token}
```

Common response:

```json
{
  "success": true,
  "errorMsg": null,
  "data": {},
  "total": null
}
```

## User APIs

### Send Login Code

```http
POST /user/code?phone=13800138000
```

### Login

```http
POST /user/login
Content-Type: application/json
```

```json
{
  "phone": "13800138000",
  "code": "123456"
}
```

Response data is the login token.

### Current User

```http
GET /user/me
authorization: {token}
```

### User Info

```http
GET /user/info/{id}
```

### Public User Profile

```http
GET /user/{id}
```

### Daily Sign-In

```http
POST /user/sign
authorization: {token}
```

### Consecutive Sign-In Count

```http
GET /user/sign/count
authorization: {token}
```

## Shop APIs

### Shop Detail

```http
GET /shop/{id}
```

### Create Shop

```http
POST /shop
Content-Type: application/json
```

### Update Shop

```http
PUT /shop
Content-Type: application/json
```

### Shops by Type

```http
GET /shop/of/type?typeId=1&current=1
```

Nearby search:

```http
GET /shop/of/type?typeId=1&current=1&x=120.149192&y=30.316078
```

### Shops by Name

```http
GET /shop/of/name?name=coffee&current=1
```

## Shop Type APIs

### Shop Type List

```http
GET /shop-type/list
```

### Clear Shop Type Cache

```http
DELETE /shop-type/cache
```

## Blog APIs

### Create Blog

```http
POST /blog
authorization: {token}
Content-Type: application/json
```

### Like or Unlike Blog

```http
PUT /blog/like/{id}
authorization: {token}
```

### My Blogs

```http
GET /blog/of/me?current=1
authorization: {token}
```

### Hot Blogs

```http
GET /blog/hot?current=1
```

### Blog Detail

```http
GET /blog/{id}
```

### Blog Like Top Users

```http
GET /blog/likes/{id}
```

### Blogs by User

```http
GET /blog/of/user?id=1&current=1
```

### Follow Feed

```http
GET /blog/of/follow?lastId=9999999999999&offset=0
authorization: {token}
```

## Follow APIs

### Follow or Unfollow

```http
PUT /follow/{id}/{isFollow}
authorization: {token}
```

Example:

```http
PUT /follow/2/true
```

### Check Follow Status

```http
GET /follow/or/not/{id}
authorization: {token}
```

### Common Follows

```http
GET /follow/common/{id}
authorization: {token}
```

## Voucher APIs

### Create Seckill Voucher

```http
POST /voucher/seckill
Content-Type: application/json
```

### Create Normal Voucher

```http
POST /voucher
Content-Type: application/json
```

### Voucher List of Shop

```http
GET /voucher/list/{shopId}
```

## Voucher Order APIs

### flash-sale order

```http
POST /voucher-order/seckill/{id}
authorization: {token}
```

Response data is the generated order ID. Order creation is asynchronous.

### Query Order Status

```http
GET /voucher-order/{id}
authorization: {token}
```

If the order is not created yet:

```json
{
  "success": true,
  "data": {
    "orderId": 123,
    "status": "PROCESSING"
  }
}
```

## Upload APIs

### Upload Blog Image

```http
POST /upload/blog
Content-Type: multipart/form-data
```

Field:

```text
file
```

### Delete Blog Image

```http
GET /upload/blog/delete?name=/blogs/1/2/demo.jpg
```
