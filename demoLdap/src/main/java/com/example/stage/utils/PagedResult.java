package com.example.stage.utils;

import java.util.List;

public record PagedResult<T>(List<T> items, String nextPageCookie) {}