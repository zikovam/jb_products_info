package com.example.jb_products_info.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Build not found")
public class BuildNotFoundException extends RuntimeException{
}
