package com.care.module.test;

import com.care.common.result.Result;
import com.care.common.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Test")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/token")
    @ApiOperation("Generate test token")
    public Result<String> generateTestToken() {
        String token = jwtUtil.generateToken(1L, "admin");
        return Result.success(token);
    }

    @GetMapping("/verify")
    @ApiOperation("Verify token")
    public Result<String> verifyToken(@RequestHeader(value = "token", required = false) String token,
                                     @RequestHeader(value = "Authorization", required = false) String auth) {
        if (token == null && auth != null) {
            if (auth.startsWith("Bearer ")) {
                token = auth.substring(7);
            }
        }
        
        if (token == null) {
            return Result.error("No token provided");
        }
        
        boolean valid = jwtUtil.validateToken(token);
        if (valid) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            return Result.success("Token valid: userId=" + userId + ", username=" + username);
        } else {
            return Result.error("Invalid token");
        }
    }
}
