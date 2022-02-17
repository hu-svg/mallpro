package com.macro.mall.security.aspect;

import com.macro.mall.security.annotation.CacheException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * @author hu
 * @create 2022/2/12
 */
@Aspect
@Slf4j
public class RedisCacheAspect {
    @Pointcut()
    public void cachePoint(){}
    @Around("cachePoint()")
    public Object doAround(ProceedingJoinPoint joinPoint)throws Throwable{
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        Object result=null;
        try {
            result=joinPoint.proceed();
        }catch(Throwable throwable){
            CacheException cacheException = method.getAnnotation(CacheException.class);
            if(cacheException!=null){
                throw new Throwable();
            }else{
                log.error(throwable.getMessage());
            }
        }

        return result;

    }
}