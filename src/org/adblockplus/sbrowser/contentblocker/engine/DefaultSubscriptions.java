/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-2015 Eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.sbrowser.contentblocker.engine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

final class DefaultSubscriptions
{
  private final List<DefaultSubscriptionInfo> subscriptions = new ArrayList<DefaultSubscriptionInfo>();
  private final List<DefaultSubscriptionInfo> linearSubscriptions = new ArrayList<DefaultSubscriptionInfo>();
  private final HashMap<String, DefaultSubscriptionInfo> urlMap = new HashMap<String, DefaultSubscriptionInfo>();
  private final List<DefaultSubscriptionInfo> adsSubscriptions = new ArrayList<DefaultSubscriptionInfo>();
  private final List<DefaultSubscriptionInfo> otherSubscriptions = new ArrayList<DefaultSubscriptionInfo>();

  private DefaultSubscriptions initialize()
  {
    this.listSubscriptions(this.linearSubscriptions);

    for (final DefaultSubscriptionInfo s : this.linearSubscriptions)
    {
      final String url = s.getUrl();
      final String type = s.getType();

      if (url.length() > 0)
      {
        this.urlMap.put(url, s);
      }

      if (type.length() == 0 || type.equals("ads"))
      {
        this.adsSubscriptions.add(s);
      }
      else if (type.equals("other"))
      {
        this.otherSubscriptions.add(s);
      }
    }

    return this;
  }

  public List<Subscription> createSubscriptions() throws IOException
  {
    final ArrayList<Subscription> subs = new ArrayList<Subscription>();
    for (DefaultSubscriptionInfo info : this.linearSubscriptions)
    {
      if (!info.getUrl().isEmpty() && (info.isComplete() || "other".equalsIgnoreCase(info.getType())))
      {
        final Subscription sub = Subscription.create(info.getUrl());
        sub.putMeta(Subscription.KEY_TITLE, info.getTitle());
        subs.add(sub);
      }
    }
    return subs;
  }

  public DefaultSubscriptionInfo getForUrl(final String url)
  {
    return this.urlMap.get(url);
  }

  public DefaultSubscriptionInfo getForUrl(final URL url)
  {
    return url != null ? this.getForUrl(url.toString()) : null;
  }

  private void listSubscriptions(final List<DefaultSubscriptionInfo> output)
  {
    for (final DefaultSubscriptionInfo s : this.subscriptions)
    {
      this.listSubscriptions(s, output);
    }
  }

  private void listSubscriptions(final DefaultSubscriptionInfo parent,
      final List<DefaultSubscriptionInfo> output)
  {
    output.add(parent);
    for (final DefaultSubscriptionInfo s : parent.variants)
    {
      this.listSubscriptions(s, output);
    }
    for (final DefaultSubscriptionInfo s : parent.supplements)
    {
      this.listSubscriptions(s, output);
    }
  }

  public static DefaultSubscriptions fromStream(final InputStream in) throws IOException
  {
    try
    {
      final SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(false);
      final SAXParser parser = factory.newSAXParser();
      final SubscriptionParser handler = new SubscriptionParser();
      parser.parse(in, handler);
      return handler.subscriptions.initialize();
    }
    catch (final ParserConfigurationException e)
    {
      throw new IOException("ParserConfigurationException: " + e.getMessage());
    }
    catch (final SAXException e)
    {
      e.printStackTrace();
      throw new IOException("SAXException: " + e.getMessage());
    }
  }

  private static class SubscriptionParser extends DefaultHandler
  {
    private boolean inSubscriptions = false;
    private boolean inVariants = false;

    private final static String KEY_SUPPLEMENTS = "supplements";
    private final static String KEY_SUBSCRIPTIONS = "subscriptions";
    private final static String KEY_SUBSCRIPTION = "subscription";
    private final static String KEY_VARIANTS = "variants";
    private final static String KEY_VARIANT = "variant";

    private final DefaultSubscriptions subscriptions = new DefaultSubscriptions();
    private final LinkedList<DefaultSubscriptionInfo> subscriptionStack = new LinkedList<DefaultSubscriptionInfo>();
    private DefaultSubscriptionInfo subscription = null;
    private DefaultSubscriptionInfo variant = null;

    @Override
    public void startElement(final String uri, final String localName, final String qName,
        final Attributes attributes) throws SAXException
    {
      super.startElement(uri, localName, qName, attributes);

      if (KEY_SUBSCRIPTIONS.equals(qName))
      {
        this.inSubscriptions = true;
      }
      else if (KEY_SUBSCRIPTION.equals(qName))
      {
        if (!this.inSubscriptions)
        {
          throw new SAXException("<subscription> outside <subscriptions>");
        }
        if (this.subscription != null)
        {
          throw new SAXException("nested <subscription>");
        }
        this.subscription = new DefaultSubscriptionInfo();
        for (int i = 0; i < attributes.getLength(); i++)
        {
          this.subscription.attributes.put(attributes.getQName(i), attributes.getValue(i));
        }
      }
      else if (KEY_VARIANTS.equals(qName))
      {
        this.inVariants = true;
      }
      else if (KEY_VARIANT.equals(qName))
      {
        if (!this.inVariants)
        {
          throw new SAXException("<variant> outside <variants>");
        }
        if (!this.inSubscriptions)
        {
          throw new SAXException("<variant> outside <subscriptions>");
        }
        if (this.subscription == null)
        {
          throw new SAXException("<variant> outside <subscription>");
        }
        if (this.variant != null)
        {
          throw new SAXException("nested <variant>");
        }
        this.variant = new DefaultSubscriptionInfo();
        this.variant.attributes.putAll(this.subscription.attributes);
        for (int i = 0; i < attributes.getLength(); i++)
        {
          this.variant.attributes.put(attributes.getQName(i), attributes.getValue(i));
        }
      }
      else if (KEY_SUPPLEMENTS.equals(qName))
      {
        if (!this.inSubscriptions)
        {
          throw new SAXException("<supplements> outside <subscriptions>");
        }
        if (this.subscription == null)
        {
          throw new SAXException("<supplements> outside <subscription>");
        }
        this.subscriptionStack.addFirst(this.subscription);
        this.subscription = null;
      }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
        throws SAXException
    {
      super.endElement(uri, localName, qName);

      if (KEY_SUBSCRIPTIONS.equals(qName))
      {
        if (!this.inSubscriptions)
        {
          throw new SAXException("</subscriptions> without <subscriptions>");
        }
        this.inSubscriptions = false;
      }
      else if (KEY_SUBSCRIPTION.equals(qName))
      {
        if (this.subscription == null)
        {
          throw new SAXException("</subscription> without <subscription>");
        }
        if (!this.subscriptionStack.isEmpty())
        {
          this.subscription.parent = this.subscriptionStack.getFirst();
          this.subscription.parent.supplements.add(this.subscription);
        }
        else
        {
          this.subscriptions.subscriptions.add(this.subscription);
        }
        this.subscription = null;
      }
      else if (KEY_VARIANTS.equals(qName))
      {
        if (!this.inVariants)
        {
          throw new SAXException("</variants> without </variants>");
        }
        this.inVariants = false;
      }
      else if (KEY_VARIANT.equals(qName))
      {
        if (this.variant == null)
        {
          throw new SAXException("</variant> without </variant>");
        }

        this.variant.parent = this.subscription;
        this.subscription.variants.add(this.variant);

        this.variant = null;
      }
      else if (KEY_SUPPLEMENTS.equals(qName))
      {
        if (this.subscriptionStack.isEmpty())
        {
          throw new SAXException("</supplements> without </supplements>");
        }
        this.subscription = this.subscriptionStack.removeFirst();
      }
    }
  }

  @Override
  public String toString()
  {
    return this.linearSubscriptions.toString();
  }
}
