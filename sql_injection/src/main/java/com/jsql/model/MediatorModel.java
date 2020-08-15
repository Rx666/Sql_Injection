package com.jsql.model;

/**
 * 容器——注册一些松耦合的组件
 */
public class MediatorModel {
    
    private static InjectionModel model;
    
    public static void register(InjectionModel model) {
        MediatorModel.model = model;
    }

    public static InjectionModel model() {
        return model;
    }
    
}
