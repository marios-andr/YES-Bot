package com.congueror.yesbot;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class ASM {

    public static void main(String[] args) throws Exception {
        ClassPool pool = new ClassPool();
        pool.appendSystemPath();
        pool.importPackage("org.slf4j");
        pool.importPackage("org.slf4j.helpers");
        pool.importPackage("org.slf4j.simple");
        pool.importPackage("org.slf4j.event");
        pool.importPackage("java.util");
        pool.importPackage("java.io");
        pool.importPackage("com.congueror.yesbot.YESBot");
        CtClass clazz = pool.get("org.slf4j.simple.SimpleLogger");
        CtMethod method = clazz.getDeclaredMethod("innerHandleNormalizedLoggingCall");

        //var info = method.getMethodInfo();
        //LocalVariableAttribute table = (LocalVariableAttribute) info.getCodeAttribute().getAttribute(LocalVariableAttribute.tag);
        //int frame = table.nameIndex(2);
        //String a = info.getConstPool().getUtf8Info(frame);


        method.setBody(
                """
                        {
                            StringBuilder buf = new StringBuilder(32);
                             
                            if (CONFIG_PARAMS.showDateTime) {
                                if (CONFIG_PARAMS.dateFormatter != null) {
                                    buf.append(getFormattedDate());
                                    buf.append(SP);
                                } else {
                                    buf.append(System.currentTimeMillis() - START_TIME);
                                    buf.append(SP);
                                }
                            }
                            
                            if (CONFIG_PARAMS.showThreadName) {
                                buf.append('[');
                                buf.append(Thread.currentThread().getName());
                                buf.append("] ");
                            }

                            if (CONFIG_PARAMS.showThreadId) {
                                buf.append(TID_PREFIX);
                                buf.append(Thread.currentThread().getId());
                                buf.append(SP);
                            }
                            
                            if (CONFIG_PARAMS.levelInBrackets)
                                buf.append('[');
                            
                            String levelStr = $1.name();
                            buf.append(levelStr);
                            if (CONFIG_PARAMS.levelInBrackets)
                                buf.append(']');
                            buf.append(SP);
                            
                            if (CONFIG_PARAMS.showShortLogName) {
                                if (shortLogName == null)
                                    shortLogName = computeShortName();
                                buf.append(String.valueOf(shortLogName)).append(" - ");
                            } else if (CONFIG_PARAMS.showLogName) {
                                buf.append(String.valueOf(name)).append(" - ");
                            }
                            
                            if ($2 != null) {
                                buf.append(SP);
                                Iterator var8 = $2.iterator();
                                
                                while(var8.hasNext()) {
                                    Marker marker = (Marker)var8.next();
                                    buf.append(marker.getName()).append(SP);
                                }
                            }
                            
                            String formattedMessage = MessageFormatter.basicArrayFormat($3, $4);
                            
                            buf.append(formattedMessage);
                            
                            YESBot.onLogMessage(buf.toString());
                            write(buf, $5);
                        }
                                """.replace("\n", ""));
        clazz.writeFile();
    }
}
