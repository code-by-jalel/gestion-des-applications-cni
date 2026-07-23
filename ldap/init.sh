#!/bin/bash

echo "Waiting for ApacheDS..."

sleep 20

ldapadd \
-x \
-H ldap://ldap:10389 \
-D "uid=admin,ou=system" \
-w secret \
-f /ldap-data/sorted_export.ldif

echo "LDAP import finished"