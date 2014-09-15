/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdfs.web;

import javax.servlet.http.HttpServletResponse;
import org.apache.hadoop.hdfs.web.HftpFileSystem.RangeHeaderInputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.hadoop.hdfs.server.namenode.StreamFile;
//import org.apache.hadoop.hdfs.web.HftpFileSystem;
import org.junit.Test;

public class TestByteRangeInputStream {
public static class MockHttpURLConnection extends HttpURLConnection {
  public MockHttpURLConnection(URL u) {
    super(u);
  }

  @Override
  public boolean usingProxy(){
    return false;
  }

  @Override
  public void disconnect() {
  }

  @Override
  public void connect() {
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream("asdf".getBytes());
  }

  @Override
  public URL getURL() {
    URL u = null;
    try {
      u = new URL("http://resolvedurl/");
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return u;
  }

  @Override
  public int getResponseCode() {
    if (responseCode != -1) {
      return responseCode;
    } else {
      if (getRequestProperty("Range") == null) {
    	  return HttpServletResponse.SC_OK;
      } else {
    	  return HttpServletResponse.SC_PARTIAL_CONTENT;
      }
    }
  }

  public void setResponseCode(int resCode) {
    responseCode = resCode;
  }

  @Override
  public String getHeaderField(String field) {
    return (field.equalsIgnoreCase(StreamFile.CONTENT_LENGTH)) ? "65535" : null;
  }
}

  public static class MockHttpUrlConnection2 extends MockHttpURLConnection {
    final InputStream mockInputStream;
    public MockHttpUrlConnection2(URL u, InputStream mockInputStream) {
      super(u);
      this.mockInputStream = mockInputStream;
    }

    @Override
    public InputStream getInputStream() {
      return mockInputStream;
    }
  }

  @Test
  public void testByteRange() throws IOException {
    URLConnectionFactory factory = mock(URLConnectionFactory.class);
    HftpFileSystem.RangeHeaderUrlOpener ospy = spy(
        new HftpFileSystem.RangeHeaderUrlOpener(factory, new URL("http://test/")));
    doReturn(new MockHttpURLConnection(ospy.getURL())).when(ospy)
        .openConnection();
    HftpFileSystem.RangeHeaderUrlOpener rspy = spy(
        new HftpFileSystem.RangeHeaderUrlOpener(factory, null));
    doReturn(new MockHttpURLConnection(rspy.getURL())).when(rspy)
        .openConnection();
    ByteRangeInputStream is = new HftpFileSystem.RangeHeaderInputStream(ospy, rspy);

    assertEquals("getPos wrong", 0, is.getPos());

    is.read();

    assertNull("Initial call made incorrectly (Range Check)", ospy
        .openConnection().getRequestProperty("Range"));

    assertEquals("getPos should be 1 after reading one byte", 1, is.getPos());

    is.read();

    assertEquals("getPos should be 2 after reading two bytes", 2, is.getPos());

    // No additional connections should have been made (no seek)

    rspy.setURL(new URL("http://resolvedurl/"));

    is.seek(100);
    is.read();

    assertEquals("Seek to 100 bytes made incorrectly (Range Check)",
        "bytes=100-", rspy.openConnection().getRequestProperty("Range"));

    assertEquals("getPos should be 101 after reading one byte", 101,
        is.getPos());

    verify(rspy, times(2)).openConnection();

    is.seek(101);
    is.read();

    verify(rspy, times(2)).openConnection();

    // Seek to 101 should not result in another request"

    is.seek(2500);
    is.read();

    assertEquals("Seek to 2500 bytes made incorrectly (Range Check)",
        "bytes=2500-", rspy.openConnection().getRequestProperty("Range"));

    ((MockHttpURLConnection) rspy.openConnection()).setResponseCode(200);
    is.seek(500);

    try {
      is.read();
      fail("Exception should be thrown when 200 response is given "
           + "but 206 is expected");
    } catch (IOException e) {
    	final String expected = String.format(HftpFileSystem.CLIENT_ERROR_FORMAT, rspy.getURL().getPath(), "HTTP_PARTIAL", HttpServletResponse.SC_OK);
    	assertTrue("Should fail because incorrect response code was sent", e.toString().contains(expected));
    }

    ((MockHttpURLConnection) rspy.openConnection()).setResponseCode(HttpServletResponse.SC_PARTIAL_CONTENT);
    is.seek(0);

    try {
      is.read();
      fail("Exception should be thrown when 206 response is given "
           + "but 200 is expected");
    } catch (IOException e) {
    	final String expected = String.format(HftpFileSystem.CLIENT_ERROR_FORMAT, rspy.getURL().getPath(), "HTTP_OK", HttpServletResponse.SC_PARTIAL_CONTENT);
    	assertTrue("Should fail because incorrect response code was sent", e.toString().contains(expected));
    }
    is.close();
  }

  @Test
  public void testPropagatedClose() throws IOException {
	  final URLConnectionFactory factory = mock(URLConnectionFactory.class);
      final InputStream mockStream = mock(InputStream.class);
      final HftpFileSystem.RangeHeaderUrlOpener ospy = spy(new HftpFileSystem.RangeHeaderUrlOpener(factory, new URL("http://test/")));
	  doReturn(new MockHttpUrlConnection2(ospy.getURL(), mockStream)).when(ospy).openConnection();
      final HftpFileSystem.RangeHeaderUrlOpener rspy = spy(new HftpFileSystem.RangeHeaderUrlOpener(factory, null));
      doReturn(new MockHttpUrlConnection2(rspy.getURL(), mockStream)).when(rspy).openConnection();

      final ByteRangeInputStream brs = spy(new RangeHeaderInputStream(ospy, rspy));

      verify(ospy, times(1)).openConnection();
      verify(rspy, times(0)).openConnection();

    int brisOpens = 0;
    int brisCloses = 0;
    int isCloses = 0;

    // stream is open, shouldn't close underlying stream
    brs.getInputStream();
    verify(brs, times(brisOpens)).openInputStream();
    verify(brs, times(brisCloses)).close();
    verify(mockStream, times(isCloses)).close();

    // seek forces a reopen, should close underlying stream
    brs.seek(1);
    brs.getInputStream();
    verify(rspy, times(1)).openConnection();
    verify(brs, times(++brisOpens)).openInputStream();
    verify(brs, times(brisCloses)).close();
    verify(mockStream, times(++isCloses)).close();

    // verify that the underlying stream isn't closed after a seek
    // ie. the state was correctly updated
    brs.getInputStream();
    verify(brs, times(brisOpens)).openInputStream();
    verify(brs, times(brisCloses)).close();
    verify(mockStream, times(isCloses)).close();

    // seeking to same location should be a no-op
    brs.seek(1);
    brs.getInputStream();
    verify(brs, times(brisOpens)).openInputStream();
    verify(brs, times(brisCloses)).close();
    verify(mockStream, times(isCloses)).close();

    // close should of course close
    brs.close();
    verify(brs, times(++brisCloses)).close();
    verify(mockStream, times(++isCloses)).close();

    // it's already closed, underlying stream should not close
    brs.close();
    verify(brs, times(++brisCloses)).close();
    verify(mockStream, times(isCloses)).close();

    // it's closed, don't reopen it
    boolean errored = false;
    try {
      brs.getInputStream();
    } catch (IOException e) {
      errored = true;
      assertEquals("Stream closed", e.getMessage());
    } finally {
      assertTrue("Read a closed steam", errored);
    }
    verify(brs, times(brisOpens)).openInputStream();
    verify(brs, times(brisCloses)).close();
    verify(mockStream, times(isCloses)).close();
  }
}
