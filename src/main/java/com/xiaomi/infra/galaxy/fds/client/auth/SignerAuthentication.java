package com.xiaomi.infra.galaxy.fds.client.auth;

import com.google.common.collect.LinkedListMultimap;
import com.xiaomi.infra.galaxy.fds.Common;
import com.xiaomi.infra.galaxy.fds.auth.signature.SignAlgorithm;
import com.xiaomi.infra.galaxy.fds.auth.signature.Signer;
import com.xiaomi.infra.galaxy.fds.client.credential.GalaxyFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.model.HttpMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SignerAuthentication implements Authentication {
  private static final Log LOG = LogFactory.getLog(SignerAuthentication.class);
  private static SignAlgorithm SIGN_ALGORITHM = SignAlgorithm.HmacSHA1;
  private final GalaxyFDSCredential credential;

  public SignerAuthentication(GalaxyFDSCredential credential){
    this.credential = credential;
  }


  @Override
  public HttpUriRequest authentication(HttpUriRequest httpRequest) throws GalaxyFDSClientException{
    String signature;
    try {
      LinkedListMultimap<String, String> headers = LinkedListMultimap.create();

      for(Header header : httpRequest.getAllHeaders()){
        headers.put(header.getName(), header.getValue());
      }
      URI uri = httpRequest.getURI();
      URI relativeUri = new URI(
          uri.toString().substring(uri.toString().indexOf('/', uri.toString().indexOf(':') + 3)));
      signature = Signer.signToBase64(HttpMethod.valueOf(httpRequest.getMethod()), relativeUri, headers,
           credential.getGalaxyAccessSecret(), SIGN_ALGORITHM);
    } catch (InvalidKeyException e){
      LOG.error("Invalid secret key spec", e);
      throw new GalaxyFDSClientException("Invalid secret key sepc", e);
    } catch (NoSuchAlgorithmException e) {
      LOG.error("Unsupported signature algorithm:" + SIGN_ALGORITHM, e);
      throw new GalaxyFDSClientException("Unsupported signature slgorithm:"
          + SIGN_ALGORITHM, e);
    } catch (Exception e) {
      throw new GalaxyFDSClientException(e);
    }
    String authString = "Galaxy-V2 " + credential.getGalaxyAccessId() + ":"
        + signature;
    httpRequest.addHeader(Common.AUTHORIZATION, authString);
    return httpRequest;
  }
}
