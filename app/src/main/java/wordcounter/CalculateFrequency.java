package wordcounter;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import org.apache.commons.lang3.ArrayUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class CalculateFrequency {
    // DoD = (DocumentFrequency/NumberArticles)*[1 - tw * (nperiodos-jperiodo)]
    static Table<String, String, Double> IssueMap = TreeBasedTable.create();

    // DoV = (TotalFrequency/NumberArticles)*[1 - tw * (nperiodos-jperiodo)]
    static Table<String, String, Double> EmergenceMap = TreeBasedTable.create();

    static Map<String, Map<String, Double>> wordTotalData = new HashMap<>();

    public static void calculate(Map<Integer, Map<String, Map<String, Integer>>> mappedwords,
            Map<Integer, List<String>> mappedLinks, double tw) {

        double Total_Frequency = 0;
        double Document_Frequency = 0;
        double Number_of_Articles = 0;
        double Number_of_Periods = mappedwords.size();
        double jPeriod = 0;

        IssueMap = TreeBasedTable.create();
        EmergenceMap = TreeBasedTable.create();

        for (int i = mappedwords.size() - 1; i >= 0; i--) {
            jPeriod++;
            Map<String, Map<String, Integer>> wordmap = mappedwords.get(i);
            Number_of_Articles = mappedLinks.get(i).size();

            for (Entry<String, Map<String, Integer>> entry : wordmap.entrySet()) {
                String word = entry.getKey();
                Map<String, Integer> sites = entry.getValue();

                Total_Frequency = sites.values().stream().reduce(0, Integer::sum);
                Document_Frequency = sites.size();

                double timeV = (1 - tw * (Number_of_Periods - jPeriod));

                double DoV = (Total_Frequency / Number_of_Articles);
                double DoD = (Document_Frequency / Number_of_Articles);

                IssueMap.put(word, jPeriod + "", DoD * timeV);
                EmergenceMap.put(word, jPeriod + "", DoV * timeV);

                addDataToWord(word, "Total Frequency", Total_Frequency);
                addDataToWord(word, "Number of Articles", Number_of_Articles);
                addDataToWord(word, "Document Frequency", Document_Frequency);
            }

        }
        CalculateIncreaseRate(jPeriod);
        ShowTables(IssueMap, jPeriod, "Total Frequency");
        ShowTables(EmergenceMap, jPeriod, "Document Frequency");
        showScPlots(jPeriod);
    }

    public static void showScPlots(double jPeriod) {
        final XYSeriesCollection Edataset = new XYSeriesCollection();
        final XYSeriesCollection Idataset = new XYSeriesCollection();

        for (Entry<String, Map<String, Double>> entry : wordTotalData.entrySet()) {

            String word = entry.getKey();
            if (entry.getValue().containsKey("EmergenceIncreaseRate")) {
                double EmergenceIncreaseRate = entry.getValue().get("EmergenceIncreaseRate");
                double absoluteAvarageTermFrquecy = entry.getValue().get("Total Frequency") / jPeriod;
                final org.jfree.data.xy.XYSeries data = new org.jfree.data.xy.XYSeries(word);
                data.add(absoluteAvarageTermFrquecy, EmergenceIncreaseRate);
                Edataset.addSeries(data);
            }
            if (entry.getValue().containsKey("IssueIncreaseRate")) {
                double IssueIncreaseRate = entry.getValue().get("IssueIncreaseRate");
                double absoluteAvarageDocumentFrquecy = entry.getValue().get("Document Frequency") / jPeriod;
                final org.jfree.data.xy.XYSeries data2 = new org.jfree.data.xy.XYSeries(word);
                data2.add(absoluteAvarageDocumentFrquecy, IssueIncreaseRate);
                Idataset.addSeries(data2);
            }
        }

        JFreeChart Escplot = ChartFactory.createScatterPlot("Emergence Map", "Avarage TermFrquecy", "IncreaseRate",
                Edataset);
        JFreeChart Iscplot = ChartFactory.createScatterPlot("Issue Map", "Avarage DocumentFrquecy", "IncreaseRate",
                Idataset);

        TickUnits standardUnits = new TickUnits();
        NumberAxis tick = new NumberAxis();
        tick.setTickUnit(new NumberTickUnit(0.01));
        standardUnits.add(tick.getTickUnit());

        XYPlot EPlot = Escplot.getXYPlot();
        XYPlot IPlot = Iscplot.getXYPlot();
        EPlot.getRangeAxis().setStandardTickUnits(standardUnits);
        IPlot.getRangeAxis().setStandardTickUnits(standardUnits);

        ChartPanel chartPanel = new ChartPanel(Escplot);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        JFrame frame = new JFrame();
        frame.add(chartPanel);
        frame.pack();
        frame.setVisible(true);

        ChartPanel chartPanel2 = new ChartPanel(Iscplot);
        chartPanel2.setPreferredSize(new java.awt.Dimension(560, 367));
        JFrame frame2 = new JFrame();
        frame2.add(chartPanel2);
        frame2.pack();
        frame2.setVisible(true);
    }

    public static void addDataToWord(String word, String dataName, Double value) {

        if (!wordTotalData.containsKey(word)) {
            wordTotalData.put(word, new HashMap<>());
        }

        Map<String, Double> wordData = wordTotalData.get(word);

        if (!wordData.containsKey(dataName)) {
            wordData.put(dataName, value);
        } else {
            wordData.put(dataName, value + wordData.get(dataName));
        }

        wordTotalData.put(word, wordData);
    }

    public static void ShowTables(Table<String, String, Double> WordSheet, double jperiods, String title) {

        Map<String, Map<String, Double>> rowMap = WordSheet.rowMap();
        List<ImmutableList<Object>> collect = rowMap.entrySet().stream()
                .map(entry -> ImmutableList.builder().add(entry.getKey()).addAll(entry.getValue().values()).build())
                .collect(Collectors.toList());

        Object[][] dados = new Object[rowMap.size()][(int) jperiods + 2];
        for (Object[] row : dados)
            Arrays.fill(row, "NO USE OF WORD");

        int i = 0;
        for (Entry<String, Map<String, Double>> entry : rowMap.entrySet()) {
            if (entry.getValue().values().size() < jperiods)
                continue;
            Object[] addAll = ArrayUtils.addAll(new Object[] { entry.getKey() }, entry.getValue().values().toArray());
            System.arraycopy(addAll, 0, dados[i], 0, addAll.length);
            // dados[i] = addAll;
            i++;
        }
        Set<String> columnKeySet = WordSheet.columnKeySet();
        Object[] colunas = ArrayUtils.insert(0, columnKeySet.toArray(), "Palavra");

        JTable table = new JTable(dados, colunas);
        JScrollPane scrollPane = new JScrollPane(table);
        // scrollPane.setRowHeaderView(table);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, table.getTableHeader());

        JFrame frame = new JFrame(title);
        frame.add(scrollPane);
        frame.setSize(new Dimension(800, 600));
        frame.setVisible(true);

        showChart(dados, title);
    }

    public static void CalculateIncreaseRate(double jPeriods) {
        Map<String, Map<String, Double>> IrowMap = IssueMap.rowMap();
        Map<String, Map<String, Double>> ErowMap = EmergenceMap.rowMap();

        for (Entry<String, Map<String, Double>> entry : IrowMap.entrySet()) {
            if (entry.getValue().values().size() < jPeriods)
                continue;
            double prod = 1;
            for (Double iterable_element : entry.getValue().values()) {
                prod *= iterable_element;
            }
            double increaseRate = Math.pow(prod, 1 / jPeriods);

            IssueMap.put(entry.getKey(), "IncreaseRate", increaseRate);
            addDataToWord(entry.getKey(), "IssueIncreaseRate", increaseRate);
        }

        for (Entry<String, Map<String, Double>> entry : ErowMap.entrySet()) {
            if (entry.getValue().values().size() < jPeriods)
                continue;
            double prod = 1;
            for (Double iterable_element : entry.getValue().values()) {
                prod *= iterable_element;
            }
            double increaseRate = Math.pow(prod, 1 / jPeriods);

            EmergenceMap.put(entry.getKey(), "IncreaseRate", increaseRate);
            addDataToWord(entry.getKey(), "EmergenceIncreaseRate", increaseRate);
        }
    }

    public static void showChart(Object[][] dados, String title) {
        XYChart chart = new XYChartBuilder().xAxisTitle("Periodos").yAxisTitle("Degree").width(1920).height(1080)
                .build();
        chart.setTitle(title);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Object[] lineData : dados) {
            double[] dado = new double[lineData.length - 2];
            int i = 0;
            String name = "";
            for (Object objects : lineData) {
                if (objects instanceof String) {
                    name = (String) objects;
                    continue;
                }
                if (i >= lineData.length - 2)
                    break;
                dado[i] = (double) objects;
                dataset.addValue(dado[i], name, "" + i);
                i++;
            }
            if (name.equals("NO USE OF WORD"))
                break;
            XYSeries series = chart.addSeries(name, null, dado);
            series.setMarker(SeriesMarkers.NONE);
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                title,
                "Periodos", "Degree",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        CategoryPlot plot = lineChart.getCategoryPlot();
        TickUnits standardUnits = new TickUnits();
        NumberAxis tick = new NumberAxis();
        tick.setTickUnit(new NumberTickUnit(0.01));
        standardUnits.add(tick.getTickUnit());
        plot.getRangeAxis().setStandardTickUnits(standardUnits);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        JFrame frame = new JFrame();
        frame.add(chartPanel);
        frame.pack();
        frame.setVisible(true);

        // new SwingWrapper<XYChart>(chart).displayChart();
    }

}
