package com.cheng.linegroup.common;

/**
 * empty response for all response as super class
 *
 * @author Cheng
 * @since 2022/12/10 下午 04:02
 */
public interface BaseResponse {
  /** if controller return this, http status will be 204 (no content) */
  BaseResponse EMPTY = new BaseResponse() {};

  /** if controller return this, http status will be 201 (created) */
  BaseResponse CREATED = new BaseResponse() {};
}
