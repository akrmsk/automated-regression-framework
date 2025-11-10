#!/bin/sh
#
# This script is the container's entrypoint. It's responsible for
# performing any runtime setup before the main application starts.

# 1. Configure DNS
# Overwrite the resolv.conf file to force the use of public DNS servers.
# This is a robust way to ensure the container has internet access.
echo "nameserver 8.8.8.8" > /etc/resolv.conf
echo "nameserver 1.1.1.1" >> /etc/resolv.conf

# 2. Start the Java Application
# This is the original command from the Dockerfile. The 'exec' command
# replaces the shell process with the Java process, which is a best practice.
exec java -jar app.jar
