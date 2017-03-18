# This file is sourced into every launch script.
# Put your machine info in the list in alphabetical order.

if [ $AUTO_MEM = "1" ]; then
   case "$HOSTNAME" in
      "jabba-hq"             )   JAVA_MAX_HEAP_SIZE=4096m;;
      "jabba-uplink"         )   JAVA_MAX_HEAP_SIZE=2048m;;
      *                      )   JAVA_MAX_HEAP_SIZE=DEFAULT;;
   esac
else
   JAVA_MAX_HEAP_SIZE=DEFAULT
fi
