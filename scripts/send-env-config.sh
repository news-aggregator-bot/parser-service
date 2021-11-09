cd ~/.ssh
echo "SendEnv SPRING_PROFILES_ACTIVE" >> tempconfig
value=$(<config)
echo "$value" >> tempconfig
cp tempconfig config
cat tempconfig
rm tempconfig