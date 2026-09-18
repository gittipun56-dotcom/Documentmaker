from docx import Document
from docx.shared import Cm, Pt
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_LINE_SPACING
from docx.enum.section import WD_SECTION
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.enum.style import WD_STYLE_TYPE
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'demo_output'; OUT.mkdir(exist_ok=True)
GARUDA=ROOT/'app/src/main/assets/garuda.png'
FONT='TH Sarabun New'


def set_run_font(run,size=16,bold=False):
    run.font.name=FONT; run.font.size=Pt(size); run.bold=bold
    rPr=run._r.get_or_add_rPr(); rFonts=rPr.rFonts
    if rFonts is None:
        rFonts=OxmlElement('w:rFonts'); rPr.insert(0,rFonts)
    for k in ('ascii','hAnsi','cs','eastAsia'): rFonts.set(qn('w:'+k),FONT)

def base(doc, top=1.5,bottom=2.0,left=3.0,right=2.0):
    s=doc.sections[0]; s.page_width=Cm(21); s.page_height=Cm(29.7)
    s.top_margin=Cm(top); s.bottom_margin=Cm(bottom); s.left_margin=Cm(left); s.right_margin=Cm(right)
    normal=doc.styles['Normal']; normal.font.name=FONT; normal.font.size=Pt(16)
    pf=normal.paragraph_format; pf.space_before=Pt(0); pf.space_after=Pt(0); pf.line_spacing=1.0
    rPr=normal.element.rPr
    if rPr is None: rPr=OxmlElement('w:rPr'); normal.element.insert(0,rPr)
    rFonts=OxmlElement('w:rFonts');
    for k in ('ascii','hAnsi','cs','eastAsia'): rFonts.set(qn('w:'+k),FONT)
    rPr.append(rFonts)

def addp(doc,text='',align=None,bold=False,first=0,left=0,right=0,line=1.0,space_before=0,space_after=0):
    p=doc.add_paragraph(); pf=p.paragraph_format
    pf.first_line_indent=Cm(first) if first else None; pf.left_indent=Cm(left) if left else None; pf.right_indent=Cm(right) if right else None
    pf.space_before=Pt(space_before); pf.space_after=Pt(space_after); pf.line_spacing=line
    if align is not None: p.alignment=align
    r=p.add_run(text); set_run_font(r,bold=bold)
    return p

def add_tab_p(doc, parts, tabs, align=None):
    p=doc.add_paragraph(); pf=p.paragraph_format; pf.space_before=Pt(0); pf.space_after=Pt(0); pf.line_spacing=1.0
    for pos in tabs:
        ts=pf.tab_stops.add_tab_stop(Cm(pos))
    if align is not None: p.alignment=align
    for i,(txt,bold) in enumerate(parts):
        if i: p.add_run('\t')
        r=p.add_run(txt); set_run_font(r,bold=bold)
    return p

def add_garuda(doc,width_cm=2.5):
    p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER; p.paragraph_format.space_after=Pt(0)
    r=p.add_run(); r.add_picture(str(GARUDA),width=Cm(width_cm))

def add_signature(doc,name,title):
    addp(doc,'',space_after=0)
    addp(doc,'(ลงชื่อ) '+name,align=WD_ALIGN_PARAGRAPH.CENTER)
    addp(doc,title,align=WD_ALIGN_PARAGRAPH.CENTER)


def external():
    d=Document(); base(d,1.5,2,3,2); add_garuda(d,2.5)
    add_tab_p(d,[('ที่ นม ๕๘๒๐๕/๑๒๓',False),('สำนักงานเทศบาลตำบลหนองหัวฟาน',False)],[10.0])
    add_tab_p(d,[('',False),('ถนนหนองหัวฟาน-หญ้าคา นม. ๓๐๒๙๐',False)],[10.0])
    addp(d,'',space_after=0)
    add_tab_p(d,[('',False),('๑๘ กันยายน ๒๕๖๙',False)],[10.0])
    addp(d,'เรื่อง   ขอเชิญประชุมเตรียมการจัดงานวันพริกและของดีอำเภอขามสะแกแสง ประจำปี ๒๕๖๙',space_before=0,space_after=0)
    addp(d,'เรียน   นายกเทศมนตรีตำบลหนองหัวฟาน',left=0.5,first=-0.5)
    addp(d,'สิ่งที่ส่งมาด้วย   ระเบียบวาระการประชุม จำนวน ๑ ชุด',left=0.5,first=-0.5)
    addp(d,'ด้วย กองการศึกษา เทศบาลตำบลหนองหัวฟาน กำหนดจัดการประชุมเตรียมการจัดงานวันพริกและของดีอำเภอขามสะแกแสง ประจำปี ๒๕๖๙ เพื่อร่วมกันกำหนดแนวทางการดำเนินงานและมอบหมายภารกิจให้เป็นไปด้วยความเรียบร้อย',first=2.5,line=1.0)
    addp(d,'ในการนี้ จึงขอเชิญท่านเข้าร่วมประชุมในวันอังคารที่ ๒๕ พฤศจิกายน ๒๕๖๘ เวลา ๑๓.๓๐ น. ณ ห้องประชุมสำนักงานเทศบาลตำบลหนองหัวฟาน',first=2.5,line=1.0)
    addp(d,'จึงเรียนมาเพื่อโปรดพิจารณาเข้าร่วมประชุมตามวัน เวลา และสถานที่ดังกล่าว',first=2.5,line=1.0)
    addp(d,'ขอแสดงความนับถือ',align=WD_ALIGN_PARAGRAPH.CENTER,space_before=6)
    add_signature(d,'นายเศรษฐการ  รวมกลาง','นายกเทศมนตรีตำบลหนองหัวฟาน')
    addp(d,'กองการศึกษา',space_before=8); addp(d,'โทร. ๐๔๔-๙๗๑-๒๒๒'); addp(d,'ผู้ประสานงาน ๐๘๔-๐๔๒-๔๙๓๖')
    d.save(OUT/'demo_หนังสือภายนอก.docx')


def memo():
    d=Document(); base(d,1.5,2,3,2); add_garuda(d,2.5)
    addp(d,'บันทึกข้อความ',align=WD_ALIGN_PARAGRAPH.CENTER,bold=True,space_after=0)
    addp(d,'ส่วนราชการ   กองการศึกษา ศาสนาและวัฒนธรรม เทศบาลตำบลหนองหัวฟาน')
    add_tab_p(d,[('ที่ นม ๕๘๒๐๕/๑๒๔',False),('วันที่ ๑๘ กันยายน ๒๕๖๙',False)],[9.0])
    addp(d,'เรื่อง   ขออนุมัติจัดประชุมเตรียมการจัดงานวันพริกและของดีอำเภอขามสะแกแสง ประจำปี ๒๕๖๙')
    addp(d,'เรียน   นายกเทศมนตรีตำบลหนองหัวฟาน')
    addp(d,'๑. เรื่องเดิม',bold=True)
    addp(d,'ตามที่เทศบาลตำบลหนองหัวฟานมีภารกิจในการส่งเสริมการท่องเที่ยว วัฒนธรรม และการมีส่วนร่วมของประชาชนในท้องถิ่น จึงมีความประสงค์จัดประชุมเตรียมการจัดงานวันพริกและของดีอำเภอขามสะแกแสง ประจำปี ๒๕๖๙',first=2.5)
    addp(d,'๒. ข้อเท็จจริง',bold=True)
    addp(d,'กองการศึกษา ศาสนาและวัฒนธรรม ได้กำหนดจัดประชุมในวันอังคารที่ ๒๕ พฤศจิกายน ๒๕๖๘ เวลา ๑๓.๓๐ น. ณ ห้องประชุมสำนักงานเทศบาลตำบลหนองหัวฟาน โดยมีหน่วยงานและผู้เกี่ยวข้องเข้าร่วมประชุมเพื่อกำหนดรายละเอียดการดำเนินงาน',first=2.5)
    addp(d,'๓. ข้อพิจารณา',bold=True)
    addp(d,'เพื่อให้การดำเนินงานเป็นไปด้วยความเรียบร้อย เห็นควรอนุมัติให้จัดประชุมตามวัน เวลา และสถานที่ดังกล่าว',first=2.5)
    addp(d,'จึงเรียนมาเพื่อโปรดพิจารณาอนุมัติ',first=2.5,space_before=6)
    add_signature(d,'นายกิติพัชญ์  รงค์พันธ์','เจ้าพนักงานธุรการปฏิบัติงาน')
    addp(d,'ความเห็นผู้อำนวยการกองการศึกษา',bold=True,space_before=10)
    addp(d,'............................................................................................................................')
    addp(d,'............................................................................................................................')
    add_signature(d,'นายนิรุธ  งอสอน','รองปลัดเทศบาล รักษาราชการแทน ผู้อำนวยการกองการศึกษา')
    d.save(OUT/'demo_บันทึกข้อความ.docx')


def command():
    d=Document(); base(d,1.5,2,3,2); add_garuda(d,2.5)
    addp(d,'คำสั่งเทศบาลตำบลหนองหัวฟาน',align=WD_ALIGN_PARAGRAPH.CENTER,bold=True)
    addp(d,'ที่  ๑๒๕ / ๒๕๖๙',align=WD_ALIGN_PARAGRAPH.CENTER)
    addp(d,'เรื่อง  แต่งตั้งคณะทำงานเตรียมการจัดงานวันพริกและของดีอำเภอขามสะแกแสง ประจำปี ๒๕๖๙',align=WD_ALIGN_PARAGRAPH.CENTER)
    addp(d,'----------------------------------------------',align=WD_ALIGN_PARAGRAPH.CENTER)
    addp(d,'เพื่อให้การจัดงานวันพริกและของดีอำเภอขามสะแกแสง ประจำปี ๒๕๖๙ เป็นไปด้วยความเรียบร้อย มีประสิทธิภาพ และเกิดประโยชน์แก่ประชาชน จึงแต่งตั้งคณะทำงานดังต่อไปนี้',first=2.5)
    addp(d,'๑. นาย.................................................. ประธานคณะทำงาน')
    addp(d,'๒. นาง.................................................. คณะทำงาน')
    addp(d,'๓. นาย.................................................. คณะทำงานและเลขานุการ')
    addp(d,'ให้คณะทำงานที่ได้รับการแต่งตั้งตามคำสั่งนี้ ปฏิบัติหน้าที่ด้วยความรับผิดชอบและประสานงานกับส่วนราชการที่เกี่ยวข้องให้การดำเนินงานเป็นไปตามวัตถุประสงค์',first=2.5)
    addp(d,'ทั้งนี้ ตั้งแต่บัดนี้เป็นต้นไป',first=2.5)
    addp(d,'สั่ง ณ วันที่ ๑๘ กันยายน พ.ศ. ๒๕๖๙',align=WD_ALIGN_PARAGRAPH.CENTER,space_before=8)
    add_signature(d,'นายเศรษฐการ  รวมกลาง','นายกเทศมนตรีตำบลหนองหัวฟาน')
    d.save(OUT/'demo_คำสั่ง.docx')

for f in (external,memo,command): f()
print('\n'.join(str(p) for p in OUT.glob('*.docx')))
