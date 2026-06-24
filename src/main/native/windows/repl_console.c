#include <jni.h>
#include <windows.h>

#ifndef ENABLE_QUICK_EDIT_MODE
#define ENABLE_QUICK_EDIT_MODE 0x0040
#endif
#ifndef ENABLE_EXTENDED_FLAGS
#define ENABLE_EXTENDED_FLAGS 0x0080
#endif
#ifndef ENABLE_VIRTUAL_TERMINAL_PROCESSING
#define ENABLE_VIRTUAL_TERMINAL_PROCESSING 0x0004
#endif

JNIEXPORT jint JNICALL Java_org_devore_Repl_enableWindowsConsoleManualEcho(JNIEnv *env, jclass clazz) {
    HANDLE input = GetStdHandle(STD_INPUT_HANDLE);
    DWORD mode;
    DWORD new_mode;
    HANDLE output;
    DWORD output_mode;
    (void) env;
    (void) clazz;
    if (input == INVALID_HANDLE_VALUE || input == NULL)
        return -1;
    if (!GetConsoleMode(input, &mode))
        return -1;
    new_mode = mode;
    new_mode &= ~(ENABLE_ECHO_INPUT | ENABLE_LINE_INPUT);
    new_mode |= ENABLE_PROCESSED_INPUT;
    new_mode |= ENABLE_EXTENDED_FLAGS;
    new_mode &= ~ENABLE_QUICK_EDIT_MODE;
    if (!SetConsoleMode(input, new_mode))
        return -1;
    output = GetStdHandle(STD_OUTPUT_HANDLE);
    if (output != INVALID_HANDLE_VALUE && output != NULL && GetConsoleMode(output, &output_mode))
        SetConsoleMode(output, output_mode | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
    return (jint) mode;
}

JNIEXPORT void JNICALL Java_org_devore_Repl_restoreWindowsConsole(JNIEnv *env, jclass clazz, jint mode) {
    HANDLE input = GetStdHandle(STD_INPUT_HANDLE);
    (void) env;
    (void) clazz;
    if (input == INVALID_HANDLE_VALUE || input == NULL)
        return;
    SetConsoleMode(input, (DWORD) mode);
}

JNIEXPORT jint JNICALL Java_org_devore_Repl_windowsConsoleColumns(JNIEnv *env, jclass clazz) {
    HANDLE output = GetStdHandle(STD_OUTPUT_HANDLE);
    CONSOLE_SCREEN_BUFFER_INFO info;
    (void) env;
    (void) clazz;
    if (output == INVALID_HANDLE_VALUE || output == NULL)
        return -1;
    if (!GetConsoleScreenBufferInfo(output, &info))
        return -1;
    return (jint) (info.srWindow.Right - info.srWindow.Left + 1);
}
