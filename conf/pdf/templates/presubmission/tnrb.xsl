<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:formatter="java:iht.utils.pdf.PdfFormatter"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>

    <xsl:template name="pre-tnrb">
        <fo:block page-break-before="always"  role="H2" xsl:use-attribute-sets="h2">
            <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'pdf.inheritance.tax.application.summary.tnrb.title', $preDeceasedName)"/>
        </fo:block>
        <fo:block>
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesTextWithParameters($translator, 'page.iht.application.tnrbEligibilty.overview.partnerEstate.questions.heading',
                    $preDeceasedName, formatter:getYearFromDate($pdfFormatter, widowCheck/dateOfPreDeceased))"/>
            </fo:block>
            <fo:table>
                <fo:table-column column-number="1" column-width="70%"/>
                <fo:table-column column-number="2" column-width="30%"/>
                <fo:table-body>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.permanentHome.question', $preDeceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isPartnerLivingInUk='true'">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.giftsMadeBeforeDeath.question', $preDeceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isGiftMadeBeforeDeath='true'">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesTextWithParameters($translator, 'page.iht.application.tnrbEligibilty.overview.giftsWithReservation.question', ' ', $preDeceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isPartnerGiftWithResToOther='true'">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.stateClaim.question', $preDeceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isStateClaimAnyBusiness='true'">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.benefitFromTrust.question', $preDeceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isPartnerBenFromTrust='true'">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesTextWithParameters($translator, 'page.iht.application.tnrbEligibilty.overview.charity.question', ' ',  $deceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isEstateBelowIhtThresholdApplied='true'">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesTextWithParameters($translator, 'page.iht.application.tnrbEligibilty.overview.jointlyOwned.question', ' ',  $deceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isJointAssetPassed='true'">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                </fo:table-body>
            </fo:table>
        </fo:block>

        <fo:block  role="H3" xsl:use-attribute-sets="h3">
            <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'site.nameDetails', $preDeceasedName)"/>
        </fo:block>
        <fo:block>
            <fo:table>
                <fo:table-column column-number="1" column-width="70%"/>
                <fo:table-column column-number="2" column-width="30%"/>
                <fo:table-body>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label" select="scala:getMessagesTextWithParameters($translator, 'iht.estateReport.tnrb.partner.married', $deceasedName, $marriedOrCivilPartnershipLabel, $preDeceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="widowCheck/widowed='true'">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.tnrbEligibilty.overview.partner.dod.question', $preDeceasedName)"/>
                        <xsl:with-param name="value" select="scala:getDateForDisplay($translator, widowCheck/dateOfPreDeceased)"/>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.name.upperCaseInitial')"/>
                        <xsl:with-param name="value" select="$preDeceasedName"/>
                    </xsl:call-template>
                    <xsl:choose>
                        <xsl:when test="increaseIhtThreshold/dateOfMarriage != ''">
                            <xsl:call-template name="table-row">
                                <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.dateOfMarriage', $marriageLabel)"/>
                                <xsl:with-param name="value" select="scala:getDateForDisplay($translator, increaseIhtThreshold/dateOfMarriage)"/>
                            </xsl:call-template>
                        </xsl:when>
                    </xsl:choose>
                </fo:table-body>
            </fo:table>
        </fo:block>

    </xsl:template>
</xsl:stylesheet>
