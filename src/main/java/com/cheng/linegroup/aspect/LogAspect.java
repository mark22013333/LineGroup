package com.cheng.linegroup.aspect;

import com.cheng.linegroup.annotation.LogApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * @author cheng
 * @since 2023/12/3 5:54 PM
 **/
@Component
@Aspect
@Slf4j
public class LogAspect {

    @Pointcut("@annotation(com.cheng.linegroup.annotation.LogApi)")
    public void baseResponse() {
    }

    @Around("baseResponse()")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long time = System.currentTimeMillis() - beginTime;
        recordLog(joinPoint, time);
        return result;
    }

    /**
     * @param joinPoint S
     **/
    private void recordLog(ProceedingJoinPoint joinPoint, long time) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogApi logApi = method.getAnnotation(LogApi.class);
        log.info("===================log start===================");
        log.info("module:{}", logApi.module());
        log.info("description:{}", logApi.description());

        // 請求的方法名稱
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        log.info("request method:{}.{}()", className, methodName);

        // 請求的參數
        Object[] args = joinPoint.getArgs();
        log.info("params:{}", args);

        // 取得request設定IP
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (requestAttributes == null) {
            log.warn("{}.{} not found request attribute, return null.", className, methodName);
            log.info("===================log end===================");
            return;
        }

        HttpServletRequest request = requestAttributes.getRequest();
        log.info("ip:{}", request.getRemoteAddr());
        log.info("execute time: {} ms", time);
        log.info("===================log end===================");
    }
}