package com.example.cablaggioservice

import android.content.Context
import android.util.Log
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.io.IOException


class CreatorExcelFile(private val context: Context, private val fileName: String, private val data: List<ModelChannelList>){

    fun createExcelFile() {
        // Crea un nuovo file Excel in formato .xlsx
        val workbook: Workbook = XSSFWorkbook()

        // Crea un foglio di lavoro nel file Excel
        val sheet: Sheet = workbook.createSheet("Sheet1")

        // Crea una riga di intestazione
        val headerRow: Row = sheet.createRow(0)
        val headers = arrayOf("", "", "CHANNEL", "", "F1 (12ch)", "F2 (11ch)", "F3 (8ch)", "F4 (8ch)", "Microfoni e aste", "AUX")
        for ((index, header) in headers.withIndex()) {
            val cell: Cell = headerRow.createCell(index)
            cell.setCellValue(header)

            // Aggiungi la formattazione per le celle dell'intestazione (grassetto, allineamento, dimensioni)
            val cellStyle = workbook.createCellStyle()
            val font = workbook.createFont()
            font.bold = true
            cellStyle.borderBottom = BorderStyle.THIN
            cellStyle.borderLeft = BorderStyle.THIN
            cellStyle.borderRight = BorderStyle.THIN
            cellStyle.borderTop = BorderStyle.THIN
            cellStyle.setFont(font)
            cellStyle.alignment = HorizontalAlignment.CENTER
            cellStyle.verticalAlignment = VerticalAlignment.CENTER
            cell.cellStyle = cellStyle
        }

        var rowNum = 1
        for (rowData in data) {
            val row: Row = sheet.createRow(rowNum)
            rowNum++

            var cellNum = 0
            for (cellData in rowData) {
                val cell: Cell = row.createCell(cellNum++)
                cell.setCellValue(cellData)

                val cellStyle = workbook.createCellStyle()
                cellStyle.wrapText = true
                cellStyle.verticalAlignment = VerticalAlignment.CENTER
                cellStyle.borderBottom = BorderStyle.THIN
                cellStyle.borderLeft = BorderStyle.THIN
                cellStyle.borderRight = BorderStyle.THIN
                cellStyle.borderTop = BorderStyle.THIN
                cellStyle.alignment = HorizontalAlignment.GENERAL

                if(cellNum == 1){
                    cellStyle.alignment = HorizontalAlignment.CENTER
                }

                when(cellNum){
                    1,2 ->{
                        val font = workbook.createFont()
                        font.bold = true
                        cellStyle.setFont(font)
                    }
                }

                cell.cellStyle = cellStyle
            }
        }

        for (columnIndex in headers.indices) {
            var maxWidthColumn = 0

            // Itera solo sulle righe che hai scritto
            for (rowIndex in 0 until rowNum) {
                val cell = sheet.getRow(rowIndex).getCell(columnIndex)
                val content = cell.toString()
                val widthRow = calculateColumnWidth(content)

                if (maxWidthColumn < widthRow) {
                    maxWidthColumn = widthRow
                }
            }
            if(columnIndex == 1){
                sheet.setColumnWidth(columnIndex, (maxWidthColumn * 1.3).toInt())
            }else{
                sheet.setColumnWidth(columnIndex, maxWidthColumn)
            }
            Log.i("msg", "columnIndex $columnIndex, maxWidthColumn $maxWidthColumn, widthColumn: ${sheet.getColumnWidth(columnIndex)}")
        }

        val fileOutputStream: FileOutputStream

        try {
            // Crea un oggetto FileOutputStream per scrivere il file
            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)

            // Scrivi il contenuto del workbook nel file
            workbook.write(fileOutputStream)

            // Chiudi il fileOutputStream
            fileOutputStream.close()

            // Ora il file Excel è stato salvato nel file system interno dell'app
        } catch (e: IOException) {
            e.printStackTrace()
        }


        // Chiudi il workbook
        workbook.close()
    }

    // Calcola la larghezza approssimativa in base al contenuto della cella
    private fun calculateColumnWidth(content: String): Int {
        val fontSizeInPoints = 12.0 // Puoi impostare il tuo valore in base alla dimensione del carattere desiderata
        val approximateCharWidth = 26 * fontSizeInPoints // Fattore approssimativo

        return when {
            content.length < 10 -> ((content.length + 1) * approximateCharWidth).toInt()
            ((content.length * 0.65 + 1) * approximateCharWidth).toInt() > MAX_WIDTH -> MAX_WIDTH
            else -> ((content.length * 0.7 + 2) * approximateCharWidth).toInt()
        }


    }

    companion object {
        private const val MAX_WIDTH = 5000
    }

}