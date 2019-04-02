#!/bin/bash
#1. park_base_url is park's server address, localhost can be set if yttps deplyed
#on the same machine, or you have to set a real address like "192.168.1.6".
#
#2.visitor_confirm_url is the public network website address for customer to confirm
#visitor.
ytpark_user='yituadmin'
/home/yituadmin/yttps/apache-apollo-1.7.1/bin/broker/bin/apollo-broker-service start
cd /home/$ytpark_user/yttps/
nohup java -jar attendance-0.0.1.jar >/dev/null &
