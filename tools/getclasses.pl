#!/usr/bin/perl

$mydroid_dir = $ARGV[0];
$installed = "/out/target/product/generic/installed-files.txt";
$sysapp_dir = "/out/target/common/obj/APPS/";
$fw_dir = "/out/target/common/obj/JAVA_LIBRARIES/";

open FILE, "<$mydroid_dir$installed" or die $!;
while (<FILE>) {
	chomp($_);
	if ($_ =~ m/  \/system\/app\/(.*).apk/) {
		$index = index($1, '/');
		$sysapp = substr($1, 0, $index);
		if (-e "$mydroid_dir$sysapp_dir$sysapp"."_intermediates/classes-full-debug.jar") {
			system("cp $mydroid_dir$sysapp_dir$sysapp"."_intermediates/classes-full-debug.jar $sysapp.jar");
		}elsif (-e "$mydroid_dir$sysapp_dir$sysapp"."_intermediates/classes.dex") {
			system("dex2jar-2.0/d2j-dex2jar.sh $mydroid_dir$sysapp_dir$sysapp"."_intermediates/classes.dex");
			system("mv classes-dex2jar.jar $sysapp.jar");
			# system("cp $mydroid_dir$sysapp_dir$sysapp"."_intermediates/classes-full-debug.jar $sysapp.jar");
		} else {
			print "$mydroid_dir$sysapp_dir$sysapp\n";
			print "???Cannot find compiled classes for APP: $sysapp???\n";
		}
	} elsif ($_ =~ m/  \/system\/framework\/(.*).jar/) {
		$fw = $1;
		if (-e "$mydroid_dir$fw_dir$fw"."_intermediates/classes-full-debug.jar") {
			system("cp $mydroid_dir$fw_dir$fw"."_intermediates/classes-full-debug.jar $fw.jar");
		} elsif (-e "$mydroid_dir$fw_dir$fw"."_intermediates/classes.jar") {
			system("cp $mydroid_dir$fw_dir$fw"."_intermediates/classes.jar $fw.jar");
		}elsif (-e "$mydroid_dir$sysapp_dir$sysapp"."_intermediates/classes.dex") {
			system("dex2jar-2.0/d2j-dex2jar.sh $mydroid_dir$sysapp_dir$sysapp"."_intermediates/classes.dex");
			system("mv classes-dex2jar.jar $sysapp.jar");
			# system("cp $mydroid_dir$sysapp_dir$sysapp"."_intermediates/classes-full-debug.jar $sysapp.jar");
		} else {
			print "???Cannot find compiled classes for Framework: $fw???\n";
		}
	}
}
close FILE;

# NFC is not installed to generic target, thus not found in the installed-files.txt
if (-e "$mydroid_dir$sysapp_dir"."Nfc_intermediates/classes-full-debug.jar") {
	system("cp $mydroid_dir$sysapp_dir"."Nfc_intermediates/classes-full-debug.jar Nfc.jar");
}
