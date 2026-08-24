package com.hf.passport;

import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Arrays;

@Aspect
public class LoggingAspect {

    @Before("execution(* com.hf.passport.ui.PassportActivity.*(..))")
    public void logMethodEntry(JoinPoint joinPoint) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取方法名
        String methodName = signature.getName();
        // 获取参数
        Object[] args = joinPoint.getArgs();
        // 打印日志
        Log.d("LoggingAspect", "enter " + methodName + "() with arguments: " + Arrays.toString(args));
    }

    @After("execution(* com.hf.passport.ui.PassportActivity.*(..))")
    public void logMethodExit(JoinPoint joinPoint) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取方法名
        String methodName = signature.getName();
        // 打印日志
//        Log.d("LoggingAspect", "leave " + methodName + "()");
    }
}
