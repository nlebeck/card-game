#!/usr/bin/perl

use warnings;
use strict;

my $usage = "./generate-messages.pl message-file";

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
        $messageHashRef->{$memberType} = $memberName;
    }
}
close(MESSAGE_FILE);

for my $key (keys %messageHash) {
    print("Message type: $key\n");
    my %memberHash = %{$messageHash{$key}};
    for my $memberKey (keys %memberHash) {
        print("    Member: $memberKey $memberHash{$memberKey}\n");
    }
}