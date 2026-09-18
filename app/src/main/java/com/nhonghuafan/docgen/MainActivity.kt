package com.nhonghuafan.docgen

import android.app.*
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private val recipientChoices = arrayOf(
        "นายกเทศมนตรีตำบลหนองหัวฟาน",
        "ผู้อำนวยการสถานศึกษาโรงเรียนหนองหัวฟาน",
        "ผู้อำนวยการศูนย์พัฒนาเด็กเล็กตำบลหนองหัวฟาน"
    )
    private lateinit var type: Spinner
    private lateinit var recipient: EditText
    private lateinit var fields: LinearLayout
    private val edits = linkedMapOf<String, EditText>()
    private val typeNames = arrayOf("หนังสือภายนอก", "บันทึกข้อความ", "คำสั่ง")
    private var selectedRecipients = mutableListOf<String>()
    private var customRecipient = ""
    private var pending: File? = null
    private var importedType: String? = null

    override fun onCreate(b: Bundle?) { super.onCreate(b); buildUi(); showFields(0) }

    private fun et(hint: String, multi: Boolean = false) = EditText(this).apply {
        this.hint = hint; textSize = 18f; setPadding(20, 10, 20, 10)
        if (multi) { minLines = 4; gravity = android.view.Gravity.TOP }
    }
    private fun label(s: String) = TextView(this).apply {
        text = s; textSize = 17f; setTextColor(Color.DKGRAY); setPadding(20, 12, 20, 4)
    }
    private fun button(textValue: String, action: () -> Unit) = Button(this).apply {
        text = textValue; setOnClickListener { action() }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(10, 8, 10, 8) }
        root.addView(TextView(this).apply {
            text = "ระบบหนังสือราชการ — ใช้งานจริง V4"
            textSize = 22f; setTextColor(Color.BLACK); setPadding(12, 8, 12, 10)
        })
        root.addView(TextView(this).apply {
            text = "สร้าง Word (.docx) แบบออฟไลน์ • ไม่ใช้ AI • แก้ต่อใน Word ได้"
            textSize = 15f; setTextColor(Color.DKGRAY); setPadding(12, 0, 12, 8)
        })

        root.addView(label("ประเภทเอกสาร"))
        type = Spinner(this)
        type.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typeNames)
        root.addView(type)

        root.addView(label("ผู้รับ / เรียน"))
        recipient = et("เลือกผู้รับหรือพิมพ์เอง", true)
        recipient.minLines = 2
        root.addView(recipient)
        root.addView(button("เลือกผู้รับหลายคน / เพิ่มผู้รับเอง") { chooseRecipients() })

        val scroll = ScrollView(this)
        fields = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(4, 0, 4, 0) }
        scroll.addView(fields)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row1.addView(button("บันทึกร่าง") { saveDraft() }, LinearLayout.LayoutParams(0, -2, 1f))
        row1.addView(button("นำ Word กลับเข้าแอป") { importWord() }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(row1)
        root.addView(button("สร้างไฟล์ Word (.docx)") { generate() })

        type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p: AdapterView<*>?) {}
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { showFields(pos) }
        }
        setContentView(root)
    }

    private fun addField(key: String, hint: String, multi: Boolean = false) {
        val e = et(hint, multi); edits[key] = e; fields.addView(label(hint)); fields.addView(e)
    }

    private fun showFields(pos: Int) {
        fields.removeAllViews(); edits.clear(); importedType = null
        when (pos) {
            0 -> {
                addField("DOC_NO", "เลขที่หนังสือ")
                addField("SENDER_ORG", "หน่วยงานผู้ส่ง")
                addField("SENDER_ADDR", "ที่อยู่หน่วยงาน")
                addField("DATE", "วันที่")
                addField("SUBJECT", "เรื่อง")
                addField("ATTACHMENT", "สิ่งที่ส่งมาด้วย")
                addField("BODY_1", "เนื้อหา ย่อหน้า 1", true)
                addField("BODY_2", "เนื้อหา ย่อหน้า 2", true)
                addField("BODY_3", "เนื้อหา ย่อหน้า 3", true)
                addField("SIGNER_NAME", "ชื่อผู้ลงนาม")
                addField("SIGNER_TITLE", "ตำแหน่งผู้ลงนาม")
                addField("CONTACT_DEPT", "ส่วนงานติดต่อ")
                addField("PHONE", "โทรศัพท์")
                addField("CONTACT", "ผู้ประสานงาน")
            }
            1 -> {
                addField("SENDER_ORG", "ส่วนราชการ")
                addField("DOC_NO", "เลขที่")
                addField("DATE", "วันที่")
                addField("SUBJECT", "เรื่อง")
                addField("OLD_MATTER", "๑. เรื่องเดิม", true)
                addField("FACTS", "๒. ข้อเท็จจริง", true)
                addField("CONSIDERATION", "๓. ข้อพิจารณา", true)
                addField("CLOSING", "ข้อความลงท้าย", true)
                addField("SIGNER_NAME", "ชื่อผู้ลงนาม")
                addField("SIGNER_TITLE", "ตำแหน่งผู้ลงนาม")
            }
            else -> {
                addField("ORG_NAME", "ชื่อหน่วยงาน")
                addField("DOC_NO", "เลขที่คำสั่ง")
                addField("SUBJECT", "เรื่อง")
                addField("PREAMBLE", "ข้อความเกริ่น / เหตุผล", true)
                addField("LIST_1", "รายการที่ ๑", true)
                addField("LIST_2", "รายการที่ ๒", true)
                addField("LIST_3", "รายการที่ ๓", true)
                addField("DUTIES", "หน้าที่ / รายละเอียด", true)
                addField("EFFECTIVE", "ข้อความมีผลใช้บังคับ", true)
                addField("DATE", "วันที่")
                addField("SIGNER_NAME", "ชื่อผู้ลงนาม")
                addField("SIGNER_TITLE", "ตำแหน่งผู้ลงนาม")
            }
        }
    }

    private fun chooseRecipients() {
        val all = recipientChoices + "ผู้รับกำหนดเอง"
        val checked = BooleanArray(all.size) { if (it < recipientChoices.size) selectedRecipients.contains(all[it]) else customRecipient.isNotBlank() }
        val input = EditText(this).apply { hint = "พิมพ์ผู้รับเพิ่มเติม"; text = customRecipient; setPadding(40, 8, 40, 8) }
        AlertDialog.Builder(this)
            .setTitle("เลือกผู้รับ / เรียน")
            .setMultiChoiceItems(all, checked) { _, which, isChecked ->
                if (which < recipientChoices.size) {
                    if (isChecked && !selectedRecipients.contains(all[which])) selectedRecipients.add(all[which])
                    if (!isChecked) selectedRecipients.remove(all[which])
                }
            }
            .setView(input)
            .setPositiveButton("ตกลง") { _, _ ->
                customRecipient = input.text.toString().trim()
                val result = selectedRecipients.toMutableList()
                if (customRecipient.isNotBlank()) result.add(customRecipient)
                recipient.setText(result.distinct().joinToString("\n"))
            }
            .setNegativeButton("ยกเลิก", null).show()
    }

    private fun collect(): MutableMap<String, String> {
        val vals = edits.mapValues { it.value.text.toString() }.toMutableMap()
        vals["RECIPIENTS"] = recipient.text.toString()
        return vals
    }

    private fun validate(vals: Map<String, String>): Boolean {
        val required = when (type.selectedItemPosition) {
            0 -> listOf("SENDER_ORG", "DATE", "SUBJECT", "BODY_1", "SIGNER_NAME")
            1 -> listOf("SENDER_ORG", "DATE", "SUBJECT", "FACTS", "SIGNER_NAME")
            else -> listOf("ORG_NAME", "DOC_NO", "SUBJECT", "PREAMBLE", "SIGNER_NAME")
        }
        val missing = required.filter { vals[it].isNullOrBlank() }
        if (missing.isNotEmpty()) {
            Toast.makeText(this, "กรุณากรอกข้อมูลสำคัญให้ครบ: ${missing.joinToString(", ")}", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun generate() {
        val pos = type.selectedItemPosition
        val vals = collect()
        if (!validate(vals)) return
        val template = when (pos) { 0 -> "templates/external_template.docx"; 1 -> "templates/memo_template.docx"; else -> "templates/command_template.docx" }
        val dir = File(cacheDir, "out").apply { mkdirs() }
        val safe = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "หนังสือราชการ_${typeNames[pos]}_$safe.docx")
        try {
            DocxTemplateEngine.create(this, template, file, typeNames[pos], vals)
            pending = file
            val i = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                putExtra(Intent.EXTRA_TITLE, file.name)
            }
            startActivityForResult(i, 900)
        } catch (e: Exception) {
            Toast.makeText(this, "สร้าง Word ไม่สำเร็จ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveDraft() {
        val vals = collect()
        val obj = JSONObject()
        obj.put("type", type.selectedItemPosition)
        obj.put("recipients", recipient.text.toString())
        edits.forEach { obj.put(it.key, it.value.text.toString()) }
        getPreferences(MODE_PRIVATE).edit().putString("draft", obj.toString()).apply()
        Toast.makeText(this, "บันทึกร่างไว้ในเครื่องแล้ว", Toast.LENGTH_SHORT).show()
    }

    private fun importWord() {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(i, 901)
    }

    private fun restoreDraft(obj: JSONObject) {
        val pos = obj.optInt("type", 0).coerceIn(0, typeNames.lastIndex)
        type.setSelection(pos)
        showFields(pos)
        recipient.setText(obj.optString("recipients", ""))
        edits.forEach { it.value.setText(obj.optString(it.key, "")) }
    }

    private fun importMetadata(uri: Uri) {
        try {
            val imported = contentResolver.openInputStream(uri)?.use { DocxTemplateEngine.importMetadata(it) }
            if (imported == null) {
                Toast.makeText(this, "ไฟล์นี้ไม่มีข้อมูลฟอร์มของระบบ จึงไม่สามารถคืนช่องกรอกอัตโนมัติได้", Toast.LENGTH_LONG).show()
                return
            }
            val pos = typeNames.indexOf(imported.type).takeIf { it >= 0 } ?: 0
            type.setSelection(pos)
            showFields(pos)
            imported.values.forEach { (k, v) -> edits[k]?.setText(v) }
            recipient.setText(imported.values["RECIPIENTS"] ?: "")
            Toast.makeText(this, "นำข้อมูลจาก Word กลับเข้าแอปแล้ว", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "นำเข้า Word ไม่สำเร็จ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data?.data == null) return
        when (requestCode) {
            900 -> contentResolver.openOutputStream(data.data!!)?.use { out -> pending?.inputStream()?.use { it.copyTo(out) } }
                .also { Toast.makeText(this, "บันทึกไฟล์ Word แล้ว", Toast.LENGTH_LONG).show() }
            901 -> importMetadata(data.data!!)
        }
    }
}
