#-*-coding:utf-8-*-
# Reference: http://blog.csdn.net/pleasecallmewhy/article/details/8932310
# Reference: http://blog.csdn.net/pleasecallmewhy/article/details/8929576

# Reference: https://wiki.python.org/moin/EscapingHtml

import string
import urllib2
# re 模块用于正则表达式的处理
import re  

# 使用urllib2库进行连接
url = 'http://www.billboard.com/charts/hot-100'
user_agent = 'Mozilla/4.0 (compatible; MSIE 5.5; Windows NT)'   
headers = { 'User-Agent' : user_agent } 
req = urllib2.Request(url, headers=headers)

response = urllib2.urlopen(req)
page = response.read()

# 首先抓取日期
date = re.findall('<time.*?datetime=".*?">(.*?)</time>',page,re.S)
# print date
# print page
# 通过正则表达式获取前100名的信息：本周排名、上周排名、歌曲名、歌手
items = re.findall('<span class="this-week">(.*?)</span>.*?<span class="last-week">(.*?)</span>.*?<div class="row-title">.*?<h2>\s+(.*?)\s+</h2>.*?<h3>.*?<a href=.*?>\s+(.*?)\s+</a>',page,re.S)

result_items = []
print '=========================== Billboard THE HOT 100 ============================'
print '                              '+date[0]
print '------------------------------------------------------------------------------'
for result_item in items:    
    # item 中第一个是本周排名    
    # item 中第二个是上周排名
    # item 中第三个是歌曲名
    # item 中第四个是歌手名
    print 'This Week: '+result_item[0]+'\t'+result_item[1]
    # 对&符号和'的特殊处理！
    result31 = result_item[3].replace("&amp;", "&")
    result21 = result_item[2].replace("&amp;", "&")
    result32 = result31.replace("&#039;", "'")
    result22 = result21.replace("&#039;", "'")
    print result32+' - '+result22
    print '------------------------------------------------------------------------------'
