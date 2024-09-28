package com.withaeng.api.common

import com.fasterxml.jackson.databind.JsonMappingException
import com.withaeng.common.exception.WithaengException
import com.withaeng.common.exception.WithaengExceptionType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class ControllerExceptionAdvice {

    private val logger: Logger = LoggerFactory.getLogger(ControllerExceptionAdvice::class.java)

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiResponse<Any>> {
        logger.error("Exception handler", ex)
        return errorResponse(WithaengExceptionType.SYSTEM_FAIL, ex.message)
    }

    @ExceptionHandler(WithaengException::class)
    fun handleMoitException(ex: WithaengException): ResponseEntity<ApiResponse<Any>> {
        logger.error("WitheangException handler", ex)
        return errorResponse(ex.httpStatusCode, ex.toApiErrorResponse())
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    protected fun handleMissingServletRequestParameterException(ex: MissingServletRequestParameterException): ResponseEntity<ApiResponse<Any>> {
        logger.error("MissingServletRequestParameterException handler", ex)
        return errorResponse(WithaengExceptionType.INVALID_INPUT, ex.message)
    }

    @ExceptionHandler(BindException::class)
    protected fun handleBindException(ex: BindException): ResponseEntity<ApiResponse<Any>> {
        logger.error("BindException handler", ex)
        return errorResponse(WithaengExceptionType.INVALID_INPUT, ex.message)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    protected fun handleMethodArgumentTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Any>> {
        logger.error("MethodArgumentTypeMismatchException handler", ex)
        return errorResponse(WithaengExceptionType.METHOD_ARGUMENT_TYPE_MISMATCH_VALUE, ex.message)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    protected fun handleHttpRequestMethodNotSupportedException(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Any>> {
        logger.error("HttpRequestMethodNotSupportedException handler", ex)
        return errorResponse(WithaengExceptionType.HTTP_REQUEST_METHOD_NOT_SUPPORTED, ex.message)
    }

    @ExceptionHandler(AccessDeniedException::class)
    protected fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ApiResponse<Any>> {
        logger.error("AccessDeniedException handler", ex)
        return errorResponse(WithaengExceptionType.ACCESS_DENIED, ex.message)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Any>> {
        logger.error("MethodArgumentNotValidException handler", e)
        val errorMessage = e.bindingResult.fieldError?.defaultMessage
        return errorResponse(WithaengExceptionType.ARGUMENT_NOT_VALID, errorMessage)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    protected fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Any>> {
        logger.error("HttpMessageNotReadableException handler", e)

        if (e.cause is JsonMappingException) {
            val jsonMappingException = e.cause as JsonMappingException
            val fieldName = jsonMappingException.path.getOrNull(0)?.fieldName
            return errorResponse(
                WithaengExceptionType.INVALID_JSON_FIELD,
                "${fieldName} 필드 값이 잘못되었습니다."
            )
        }
        return errorResponse(WithaengExceptionType.JSON_PARSE_ERROR, "잘못된 데이터가 요청되었습니다.")
    }

    private fun WithaengException.toApiErrorResponse() = ApiErrorResponse(
        code = errorCode,
        message = message,
    )

    private fun errorResponse(
        exceptionType: WithaengExceptionType,
        message: String?,
    ) = errorResponse(
        exceptionType.httpStatusCode,
        ApiErrorResponse(exceptionType.name, message)
    )

    private fun errorResponse(status: Int, errorResponse: ApiErrorResponse) =
        errorResponse(HttpStatus.valueOf(status), errorResponse)

    private fun errorResponse(status: HttpStatus, errorResponse: ApiErrorResponse): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity.status(status)
            .body(
                ApiResponse(
                    success = false,
                    data = null,
                    error = errorResponse,
                )
            )
}