
all:
	./gradlew aR 
	jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ~/android_release/my-release-key.keystore BitCurrency/build/apk/BitCurrency-release-unsigned.apk  haneul 
	~/adt-bundle-mac-x86_64-20130717/sdk/tools/zipalign -v 4 BitCurrency/build/apk/BitCurrency-release-unsigned.apk BitCurrency.apk 

