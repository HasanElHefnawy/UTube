package com.example.utube.extractor;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.util.Objects;

class CipherManager {
    private static final String TAG = "zzzzz CipherManager";
    private static final String RegexDesipherFunctionCode = "\\{[a-zA-Z]{1,}=[a-zA-Z]{1,}\\.split\\(\"\"\\).*?\\};";
    private static final String RegexVarName = "[a-zA-Z0-9$]{2}\\.[a-zA-Z0-9$]{2}\\([a-zA-Z]\\,(\\d\\d|\\d)\\)";
    private static String cachedDechiperFunction = null;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String getDecipherCode(String Basejs) {
        String DechipherCode;
        String DecipherFun = "decipher=function(a)" + RegexUtils.matchGroup(RegexDesipherFunctionCode, Basejs);
        String RawName = Objects.requireNonNull(RegexUtils.matchGroup(RegexVarName, DecipherFun)).replace("$", "\\$");
        String RealVarName = RawName.split("\\.")[0];
        String regexFindVarCode = "var\\s" + RealVarName + "=.*?\\};";    // Word 1
        String varCode = RegexUtils.matchGroup(regexFindVarCode, Basejs);
        DechipherCode = DecipherFun + "\n" + varCode;
        Log.e(TAG, DechipherCode);
        return DechipherCode;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static String dechiperSig(String sig, String playerUrl) throws IOException {
        if (cachedDechiperFunction == null) {
            cachedDechiperFunction = getDecipherCode(getPlayerCode(playerUrl));
        }
        return RhinoEngine(sig);
    }

    private static String getPlayerCode(String playerUrl) throws IOException {
        return HTTPUtility.downloadPageSource(playerUrl);
    }

    private static String RhinoEngine(String s) {
        Context rhino = Context.enter();
        rhino.setOptimizationLevel(-1);
        try {
            Scriptable scope = rhino.initStandardObjects();
            rhino.evaluateString(scope, cachedDechiperFunction, "JavaScript", 1, null);
            Object obj = scope.get("decipher", scope);
            if (obj instanceof Function) {
                Function jsFunction = (Function) obj;
                Object jsResult = jsFunction.call(rhino, scope, scope, new Object[]{s});
                return Context.toString(jsResult);
            }
        } finally {
            Context.exit();
        }
        return s;
    }
}
