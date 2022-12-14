package com.sharespot.service;

import java.util.Map;

public interface JwtService {
	
	<T> String create(String key, T data, String subject);
	Map<String,Object> get();
	int getUserId();
	boolean isUsable(String jwt);

}
