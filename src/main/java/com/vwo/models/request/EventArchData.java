/**
 * Copyright 2024-2025 Wingify Software Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vwo.models.request;

import com.vwo.models.request.visitor.Visitor;

public class EventArchData {


  private String msgId;
  private String visId;
  private Long sessionId;
  private Event event;
  private Visitor visitor;
  private String visitor_ua;
  private String visitor_ip;

  public String getMsgId() {
    return msgId;
  }

  public void setMsgId(String msgId) {
    this.msgId = msgId;
  }

  public String getVisId() {
    return visId;
  }

  public void setVisId(String visId) {
    this.visId = visId;
  }

  public Long getSessionId() {
    return sessionId;
  }

  public void setSessionId(Long sessionId) {
    this.sessionId = sessionId;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }

  public Visitor getVisitor() {
    return visitor;
  }

  public void setVisitor(Visitor visitor) {
    this.visitor = visitor;
  }

  public String getVisitor_ua() {
    return visitor_ua;
  }

  public void setVisitor_ua(String visitor_ua) {
    this.visitor_ua = visitor_ua;
  }

  public String getVisitor_ip() {
    return visitor_ip;
  }

  public void setVisitor_ip(String visitor_ip) {
    this.visitor_ip = visitor_ip;
  }
}
