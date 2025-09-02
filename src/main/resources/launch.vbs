Set objShell = CreateObject("WScript.Shell")
args = WScript.Arguments.Count
if args = 0 then
    ' 无参数时启动GUI（隐藏窗口）
    objShell.Run "javaw -jar " & Chr(34) & objShell.CurrentDirectory & "\c-toolkit-1.0-SNAPSHOT.jar" & Chr(34), 0
else
    ' 有参数时启动CLI（显示窗口）
    cmd = "java -jar " & Chr(34) & objShell.CurrentDirectory & "\c-toolkit-1.0-SNAPSHOT.jar" & Chr(34)
    for i = 0 to args - 1
        cmd = cmd & " " & Chr(34) & WScript.Arguments(i) & Chr(34)
    next
    objShell.Run cmd, 1
end if