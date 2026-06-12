--- 秒杀脚本 ---
----Key
---Key1：库存： seckill:stock:{seckillId}
local stockKey = KEYS[1];
---Key2：用户购买标记：seckill:userPay:{seckillId}
local userKey = KEYS[2];

----ARGV
---用户Id：userId
local userId = ARGV[1];
---购买数量
local payNum = ARGV[2];

-- 判断剩余库存是否满足购买条件
local stock = redis.call("get", stockKey);
if not stock or tonumber(stock) < tonumber(payNum) then
    return { -1, "库存不足" };
end

-- 判断用户是否重复秒杀
local user = redis.call("sismember", userKey, userId);
if user == 1 then
    return { -2, "用户重复秒杀" };
end

-- 预扣减库存
redis.call("incrby", stockKey, -payNum);
-- 记录用户
redis.call("sadd", userKey, userId);

-- 返回成功结果
return { 0, "秒杀成功" };
