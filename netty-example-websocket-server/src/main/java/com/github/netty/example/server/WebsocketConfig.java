package com.github.netty.example.server;

import com.github.netty.websocket.NettyRequestUpgradeStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.messaging.DefaultSimpUserRegistry;
import org.springframework.web.socket.server.RequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Websocket配置
 * @author 84215
 */
@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocket
public class WebsocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    /**
     * 请求协议升级的策略, 这里选用netty
     * @return 策略
     */
    public RequestUpgradeStrategy requestUpgradeStrategy(){
//        return new JettyRequestUpgradeStrategy();
//        return new TomcatRequestUpgradeStrategy();
        return new NettyRequestUpgradeStrategy();
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //添加一个/my-websocket端点，客户端就可以通过这个端点来进行连接；
        StompWebSocketEndpointRegistration endpoint = registry.addEndpoint("/my-websocket");

        //这里放入一个握手的处理器,可以处理自定义握手期间的事情,重写父类方法即可 选用netty的协议升级策略
        endpoint.setHandshakeHandler(new DefaultHandshakeHandler(requestUpgradeStrategy()){
            //这里获取首次握手的身份
            @Override
            protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                String token = request.getHeaders().getFirst("access_token");
                return () -> "账号-" + token;
            }
        });

        //setAllowedOrigins(*)设置跨域.  withSockJS(*)添加SockJS支持
        endpoint.setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //定义了一个客户端订阅地址的前缀信息，也就是客户端接收服务端发送消息的前缀信息
        registry.enableSimpleBroker("/topic/");
        //api全局的前缀名
        registry.setApplicationDestinationPrefixes("/app");
        // 点对点使用的订阅前缀（客户端订阅路径上会体现出来），不设置的话，默认也是/user/ ， 如果设置了全局前缀效果为 /app/user/xxx
        registry.setUserDestinationPrefix("/user/");
    }

    /**
     * 负责管理用户信息
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(SimpUserRegistry.class)
    public SimpUserRegistry userRegistry(){
        return new DefaultSimpUserRegistry();
    }

}
