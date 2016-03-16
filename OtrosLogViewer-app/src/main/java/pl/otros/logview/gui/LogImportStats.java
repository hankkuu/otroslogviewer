/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.gui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class LogImportStats extends AbstractTableModel {

  private final String fileName;

  private final NumberFormat speedFormat = NumberFormat.getInstance();
  private final NumberFormat downloadedFormat = NumberFormat.getIntegerInstance();
  private final LinkedList<Long> dates;
  private final LinkedList<Long> loaded;
  private static final int STATS_SIZE = 100;
  private final DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.getDefault());
  private long max;
  private long downlaoded;
  private long timeleft;
  private float speedKbPerSecond;
  private final Date eta = new Date();
  private final String[] rowsNames = {"File", "Downloaded", "ETA", "Time left", "Speed [kb/s]"};

  public LogImportStats(String fileName) {
    super();
    this.fileName = fileName;
    dates = new LinkedList<>();
    loaded = new LinkedList<>();
    speedFormat.setGroupingUsed(true);
    speedFormat.setMaximumFractionDigits(2);
    speedFormat.setMinimumFractionDigits(2);

  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public int getRowCount() {
    return rowsNames.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (columnIndex == 0) {
      return rowsNames[rowIndex];
    }
    Object value = "?";
    switch (rowIndex) {
      case 0:
        value = fileName;
        break;
      case 1:
        value = downloadedFormat.format(downlaoded);
        break;
      case 2:
        value = dateFormat.format(eta);
        break;
      case 3:
        value = timeleft + "s";
        break;
      case 4:
        value = speedFormat.format(speedKbPerSecond);
        break;
      default:

    }
    return value;
  }

  public void updateStats(long date, long loaded, long max) {
    try {
      this.max = max;
      dates.addFirst(date);
      downlaoded = loaded;
      this.loaded.addFirst(loaded);
      if (dates.size() > STATS_SIZE) {
        dates.removeLast();
        this.loaded.removeLast();
      }
      updateEtaAndSpeed();
    } catch (Exception e) {
      // LOGGER.error("GOD, why? " + e.getMessage());
    }
    SwingUtilities.invokeLater(() -> fireTableRowsUpdated(0, getRowCount()));
  }

  private void updateEtaAndSpeed() {
    long now = dates.getFirst();
    long past = dates.getLast();
    long loadedKbNow = loaded.getFirst();
    long loadedKbPast = loaded.getLast();
    long kiloBytesRemaining = max - loadedKbNow;
    // speed kb/
    speedKbPerSecond = (float) ((double) (loadedKbNow - loadedKbPast)) / ((now - past) / 1000);
    timeleft = (long) (kiloBytesRemaining / speedKbPerSecond);
    eta.setTime(now + timeleft * 1000);

  }
}
