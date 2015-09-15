package com.pinplanet.pintact.utility;

/**
 * Created by pranab on 8/14/14.
 */
public abstract class LoadingAsyncTask implements PostServiceExecuteTask
{
  @Override
  public void run(int statusCode, String result) {

  }
  public abstract void runTask();

}
