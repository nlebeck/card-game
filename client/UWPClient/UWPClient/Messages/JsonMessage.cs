﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;
using System.Threading.Tasks;

namespace UWPClient.Messages
{
    [DataContract]
    public class JsonMessage
    {
        [DataMember]
        public string messageType;
    }
}
