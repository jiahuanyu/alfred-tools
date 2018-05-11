if [ "$1" = "success" ]; then
    osascript -e 'display notification "上传成功" with title "提示"'
elif [ "$1" = "error" ]; then
    osascript -e 'display notification "上传或者获取图片失败" with title "错误"'
fi
