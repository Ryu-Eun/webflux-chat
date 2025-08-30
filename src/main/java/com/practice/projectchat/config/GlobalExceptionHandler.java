package com.practice.projectchat.config;

import com.practice.projectchat.exception.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    record ErrorResponse(String code, String message, Map<String, String> details){}

    // 400: 요청 바인딩/검증 실패 (@Valid 관련)
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBind(WebExchangeBindException ex) {
        var details = ex.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),          // password, loginId 같은 필드명
                        fe -> fe.getDefaultMessage(), // 검증 메시지
                        (a,b) -> a                 // 같은 필드에 여러 에러가 있으면 첫번째만 유지
                ));
        var body = new ErrorResponse("VALIDATION_ERROR", "입력값이 올바르지 않습니다.", details);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }

    // 409: 명시적으로 던진 중복/상태 충돌 등
    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalState(IllegalStateException ex) {
        var body = new ErrorResponse("CONFLICT", ex.getMessage(), null);
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(body));
    }


    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBadCred(BadCredentialsException ex) {
        String code = ex.getMessage() != null ? ex.getMessage() : "INVALID_CREDENTIALS";
        var body = new ErrorResponse(code, "인증에 실패했습니다.", null);
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body)); // 401
    }

    @ExceptionHandler({LockedException.class, DisabledException.class})
    public Mono<ResponseEntity<ErrorResponse>> handleAccountState(RuntimeException ex) {
        String code = switch (ex.getMessage()) {
            case "ACCOUNT_BLOCKED" -> "ACCOUNT_BLOCKED";
            case "ACCOUNT_DEACTIVATED" -> "ACCOUNT_DEACTIVATED";
            default -> "FORBIDDEN";
        };
        var body = new ErrorResponse(code, "로그인할 수 없는 상태의 계정입니다.", null);
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(body)); // 403
    }

    // 409: DB 유니크 제약 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDataIntegrity(DataIntegrityViolationException ex) {
        var body = new ErrorResponse("CONFLICT", "데이터 제약 조건 위반", Map.of("reason","DATA_INTEGRITY"));
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(body));
    }

    //TODO: 이 아래부터는 CustomException 적용함. 이 위에 CustomException으로 처리 안한것들 나중에 수정해야됨

    @ExceptionHandler(AlreadyFriendException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAlreadyFriends(AlreadyFriendException ex) {
        var body = new ErrorResponse("ALREADY_FRIENDS", ex.getMessage(), null);
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(body));
    }

    @ExceptionHandler(DuplicateFriendRequestException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDuplicateFriendRequest(DuplicateFriendRequestException ex) {
        var body = new ErrorResponse("DUPLICATE_REQUEST", ex.getMessage(), null);
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(body));
    }

    @ExceptionHandler(FriendRequestNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleFriendRequestNotFound(FriendRequestNotFoundException ex) {
        var body = new ErrorResponse("FRIEND_REQUEST_NOT_FOUND", ex.getMessage(), null);
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(body));
    }

    @ExceptionHandler(FriendRequestAlreadyHandledException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleFriendRequestAlreadyHandled(FriendRequestAlreadyHandledException ex) {
        var body = new ErrorResponse("FRIEND_REQUEST_ALREADY_HANDLED", ex.getMessage(), null);
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(body));
    }

    @ExceptionHandler(FriendRequestPermissionException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleFriendRequestPermission(FriendRequestPermissionException ex) {
        var body = new ErrorResponse("FRIEND_REQUEST_FORBIDDEN", ex.getMessage(), null);
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(body));
    }


    // 그 밖의 예외 -> 500
    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<ErrorResponse>> handleOthers(Throwable ex) {
        var body = new ErrorResponse("INTERNAL_ERROR", "서버 오류가 발생했습니다.", null);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
    }



}