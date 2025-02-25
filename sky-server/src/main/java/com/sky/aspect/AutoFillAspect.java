package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自动填充切面
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect  {

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){    }

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("自动填充切面执行");
        // 获取当前正在执行的方法对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);// 获取方法上的注解
        OperationType operationType = autoFill.value();// 获取注解上的操作类型

        // 获取实体对象
        Object[] args = joinPoint.getArgs();
        if(args.length == 0){
            return;
        }
        Object entity = args[0];

        //准备赋值数据
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();

        if(OperationType.UPDATE == operationType){
            //更新操作
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //执行赋值操作
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }else if(OperationType.INSERT == operationType){
            //插入操作
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //执行赋值操作
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,id);
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
