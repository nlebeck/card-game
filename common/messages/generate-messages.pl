#!/usr/bin/perl

use warnings;
use strict;

my $usage = "./generate-messages.pl message-file";

my $javaPackage = "niellebeck.cardgameserver.messages";
my $csharpNamespace = "UWPClient.Messages";

my $javaOutputDir = "../../server/CardGameServer/src/main/java/niellebeck/cardgameserver/messages";
my $csharpOutputDir = "../../client/UWPClient/UWPClient/Messages";

if (@ARGV != 1) {
    die $usage;
}

my $messageFile = $ARGV[0];

my %messageHash; # Maps each message type to a reference to an array containing its members,
                 # where each entry is "[type] [name]"

my $currentMessageType;

open(MESSAGE_FILE, $messageFile);
while(<MESSAGE_FILE>) {
    if ($_ =~ /(\w+):\s*$/) {
        my $messageType = $1;
        $messageHash{$messageType} = [];
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
        my $memberArrayRef = $messageHash{$currentMessageType};
        push(@$memberArrayRef, "$memberType $memberName");
    }
}
close(MESSAGE_FILE);

# Generate Java message source files
for my $messageType (keys %messageHash) {
    open(MESSAGE_FILE, "> $javaOutputDir/$messageType.java");
    print(MESSAGE_FILE "package $javaPackage;\n");
    print(MESSAGE_FILE "\n");
    print(MESSAGE_FILE "public class $messageType extends JsonMessage {\n");
    my @memberArray = @{$messageHash{$messageType}};
    for (my $i = 0; $i < @memberArray; $i++) {
        my ($memberType, $memberName) = split(/\s+/, $memberArray[$i]);
        if ($memberType eq "string") {
            $memberType = "String";
        }
        elsif ($memberType eq "string[]") {
            $memberType = "String[]";
        }
        print(MESSAGE_FILE "    public $memberType $memberName;\n");
    }
    print(MESSAGE_FILE "}\n");
    close(MESSAGE_FILE);
}

# Generate Java JsonMessageFactory source file
open(JAVA_FACTORY_FILE, "> $javaOutputDir/JsonMessageFactory.java");
print(JAVA_FACTORY_FILE "package $javaPackage;\n");
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
print(JAVA_FACTORY_FILE "\n");
for my $messageType (keys %messageHash) {
    my @memberArray = @{$messageHash{$messageType}};

    print(JAVA_FACTORY_FILE "    public static $messageType create$messageType(");
    for (my $i = 0; $i < @memberArray - 1; $i++) {
        my ($memberType, $memberName) = split(/\s+/, $memberArray[$i]);
        if ($memberType eq "string") {
            $memberType = "String";
        }
        elsif ($memberType eq "string[]") {
            $memberType = "String[]";
        }
        print(JAVA_FACTORY_FILE "$memberType $memberName, ");
    }
    my ($lastMemberType, $lastMemberName) = split(/\s+/, $memberArray[@memberArray - 1]);
    if ($lastMemberType eq "string") {
        $lastMemberType = "String";
    }
    elsif ($lastMemberType eq "string[]") {
        $lastMemberType = "String[]";
    }
    print(JAVA_FACTORY_FILE "$lastMemberType $lastMemberName) {\n");

    print(JAVA_FACTORY_FILE "        $messageType message = new $messageType();\n");
    print(JAVA_FACTORY_FILE "        message.messageType = \"$messageType\";\n");
    for (my $i = 0; $i < @memberArray; $i++) {
        my ($memberType, $memberName) = split(/\s+/, $memberArray[$i]);
        print(JAVA_FACTORY_FILE "        message.$memberName = $memberName;\n");
    }
    print(JAVA_FACTORY_FILE "        return message;\n");
    print(JAVA_FACTORY_FILE "    }\n");
    print(JAVA_FACTORY_FILE "\n");
}
print(JAVA_FACTORY_FILE "}\n");
close(JAVA_FACTORY_FILE);

# Generate C# message source files
for my $messageType (keys %messageHash) {
    open(MESSAGE_FILE, "> $csharpOutputDir/$messageType.cs");
    print(MESSAGE_FILE "namespace $csharpNamespace\n");
    print(MESSAGE_FILE "{\n");
    print(MESSAGE_FILE "\n");
    print(MESSAGE_FILE "    public class $messageType : JsonMessage\n");
    print(MESSAGE_FILE "    {\n");
    my @memberArray = @{$messageHash{$messageType}};
    for (my $i = 0; $i < @memberArray; $i++) {
        my ($memberType, $memberName) = split(/\s+/, $memberArray[$i]);
        print(MESSAGE_FILE "        public $memberType $memberName;\n");
    }
    print(MESSAGE_FILE "    }\n");
    print(MESSAGE_FILE "}\n");
    close(MESSAGE_FILE);
}

# Generate C# JsonMessageFactory source file
open(CSHARP_FACTORY_FILE, "> $csharpOutputDir/JsonMessageFactory.cs");
print(CSHARP_FACTORY_FILE "namespace $csharpNamespace\n");
print(CSHARP_FACTORY_FILE "{\n");
print(CSHARP_FACTORY_FILE "\n");
print(CSHARP_FACTORY_FILE "    using Newtonsoft.Json;\n");
print(CSHARP_FACTORY_FILE "\n");
print(CSHARP_FACTORY_FILE "    public class JsonMessageFactory\n");
print(CSHARP_FACTORY_FILE "    {\n");
print(CSHARP_FACTORY_FILE "        public static JsonMessage DeserializeJsonMessage(string message)\n");
print(CSHARP_FACTORY_FILE "        {\n");
print(CSHARP_FACTORY_FILE "            JsonMessage jsonMessage = JsonConvert.DeserializeObject<JsonMessage>(message);\n");
print(CSHARP_FACTORY_FILE "            string messageType = jsonMessage.messageType;\n");
print(CSHARP_FACTORY_FILE "\n");
for my $messageType (keys %messageHash) {
    print(CSHARP_FACTORY_FILE "            if (messageType.Equals(\"$messageType\"))\n");
    print(CSHARP_FACTORY_FILE "            {\n");
    print(CSHARP_FACTORY_FILE "                return JsonConvert.DeserializeObject<$messageType>(message);\n");
    print(CSHARP_FACTORY_FILE "            }\n");
}
print(CSHARP_FACTORY_FILE "\n");
print(CSHARP_FACTORY_FILE "            return null;\n");
print(CSHARP_FACTORY_FILE "        }\n");
print(CSHARP_FACTORY_FILE "\n");
for my $messageType (keys %messageHash) {
    my @memberArray = @{$messageHash{$messageType}};

    print(CSHARP_FACTORY_FILE "        public static $messageType Create$messageType(");
    for (my $i = 0; $i < @memberArray - 1; $i++) {
        my ($memberType, $memberName) = split(/\s+/, $memberArray[$i]);
        print(CSHARP_FACTORY_FILE "$memberType $memberName, ");
    }
    my ($lastMemberType, $lastMemberName) = split(/\s+/, $memberArray[@memberArray - 1]);
    print(CSHARP_FACTORY_FILE "$lastMemberType $lastMemberName)\n");
    print(CSHARP_FACTORY_FILE "        {\n");

    print(CSHARP_FACTORY_FILE "            $messageType message = new $messageType();\n");
    print(CSHARP_FACTORY_FILE "            message.messageType = \"$messageType\";\n");
    for (my $i = 0; $i < @memberArray; $i++) {
        my ($memberType, $memberName) = split(/\s+/, $memberArray[$i]);
        print(CSHARP_FACTORY_FILE "            message.$memberName = $memberName;\n");
    }
    print(CSHARP_FACTORY_FILE "            return message;\n");
    print(CSHARP_FACTORY_FILE "        }\n");
    print(CSHARP_FACTORY_FILE "\n");
}
print(CSHARP_FACTORY_FILE "    }\n");
print(CSHARP_FACTORY_FILE "}\n");
close(CSHARP_FACTORY_FILE);
