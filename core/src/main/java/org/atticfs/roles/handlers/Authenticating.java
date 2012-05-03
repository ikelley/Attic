/*
 * Copyright 2004 - 2012 Cardiff University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atticfs.roles.handlers;

import org.atticfs.channel.ChannelData;

/**
 * Classes that implement this interface define an authentication key
 * for particular types of request (determined by the ChannelData)
 * A null value returned means no authentication is required.
 * If a value is returned, this key is matched against the security context
 * and the certificates supplied in the requrest.
 *
 * 
 */

public interface Authenticating {

    public String getAuthenticationKey(ChannelData context);

}
