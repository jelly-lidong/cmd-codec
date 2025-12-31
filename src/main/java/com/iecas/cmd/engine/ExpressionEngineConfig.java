package com.iecas.cmd.engine;

import com.iecas.cmd.util.Constants;
import lombok.Data;

/**
 * 表达式引擎配置
 */
@Data
public class ExpressionEngineConfig {
    private boolean cacheEnabled = true;
    private int maxCacheSize = 1000;
    private boolean optimizeEnabled = true;
    private boolean traceEnabled = false;
    private int maxLoopCount = Constants.DEFAULT_MAX_LOOP_COUNT;
}