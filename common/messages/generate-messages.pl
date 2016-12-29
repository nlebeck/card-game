#!/usr/bin/perl

use warnings;
use strict;

my $usage = "./generate-messages.pl message-file";

my $javaPackage = "niellebeck.cardgame.messages";

if (@ARGV != 1) {
    die $usage;
}

my $messageFile = $ARGV[0];

my %messageHash;

my $currentMessageType;

open(MESSAGE_FILE, $messageFile);
while(<MESSAGE_FILE>) {
    if ($_ =~ /(\w+):\s*$/) {
        my $messageType = $1;
        $messageHash{$messageType} = {};
        $currentMessageType = $messageType;
    }
    elsif ($_ =~ /\w+/) {
        my $trimmedLine = "";
        if ($_ =~ /^\s*(.*)\s*$/) {
            $trimmedLine = $1;
        }
        my @lineSplit = split(/\s+/, $trimmedLine);
        if (@lineSplit != 2) {
            die "Message formatting error at the following line: $_";
        }
        my $memberType = $lineSplit[0];
        my $memberName = $lineSplit[1];
        if ($memberType ne "int[]"
            && $memberType ne "string[]"
            && $memberType ne "int"
            && $memberType ne "string") {
            die "Unrecognized type: $memberType";
        }
        my $messageHashRef = $messageHash{$currentMessageType};
        $messageHashRef->{$memberName} = $memberType;
    }
}
close(MESSAGE_FILE);

# Generate Java message source files
for my $messageType (keys %messageHash) {
    open(MESSAGE_FILE, "> generated-java/$messageType.java");
    print(MESSAGE_FILE "package $javaPackage\n");
    print(MESSAGE_FILE "\n");
    print(MESSAGE_FILE "public class $messageType extends JsonMessage {\n");
    my %memberHash = %{$messageHash{$messageType}};
    for my $memberName (keys %memberHash) {
        print(MESSAGE_FILE "    public $memberHash{$memberName} $memberName;\n");
    }
    print(MESSAGE_FILE "}\n");
    close(MESSAGE_FILE);
}

# Generate Java JsonMessageFactory source file
open(JAVA_FACTORY_FILE, "> generated-java/JsonMessageFactory.java");
print(JAVA_FACTORY_FILE "package $javaPackage\n");
print(JAVA_FACTORY_FILE "\n");
print(JAVA_FACTORY_FILE "import com.google.gson.Gson;\n");
print(JAVA_FACTORY_FILE "\n");
print(JAVA_FACTORY_FILE "public class JsonMessageFactory {\n");
print(JAVA_FACTORY_FILE "    public static JsonMessage deserializeJsonMessage(String message) {\n");
print(JAVA_FACTORY_FILE "        Gson gson = new Gson();\n");
print(JAVA_FACTORY_FILE "        JsonMessage jsonMessage = gson.fromJson(message, JsonMessage.class);\n");
print(JAVA_FACTORY_FILE "        String messageType = jsonMessage.messageType;\n");
print(JAVA_FACTORY_FILE "\n");
for my $messageType (keys %messageHash) {
    print(JAVA_FACTORY_FILE "        if (messageType.equals(\"$messageType\")) {\n");
    print(JAVA_FACTORY_FILE "            return gson.fromJson(message, $messageType.class);\n");
    print(JAVA_FACTORY_FILE "        }\n");
}
print(JAVA_FACTORY_FILE "\n");
print(JAVA_FACTORY_FILE "        return null;\n");
print(JAVA_FACTORY_FILE "    }\n");
close(JAVA_FACTORY_FILE);